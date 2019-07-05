import java.io.File
import java.net.{URI, URL}

import cats._
import cats.effect._
import cats.implicits._
import cats.syntax._
import cats.data._
import com.monovore.decline._
import DeclineHelpers._
import io.idml._
import io.idml.utils.DocumentValidator
import io.idmlrepl.Repl
import io.idml.hashing.HashingFunctionResolver
import io.idml.jsoup.JsoupFunctionResolver
import io.idml.server.Server
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import org.http4s.server.blaze.BlazeBuilder
import io.idml.server.WebsocketServer
import io.idml.utils.Tracer.Annotator

import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

object IdmlTools {
  implicit val urlArgument = new Argument[URL] {
    override def read(string: String): ValidatedNel[String, URL] =
      Try {
        new URL(string)
      }.toEither.leftMap(_.getMessage).toValidatedNel
    override def defaultMetavar: String = "URL"
  }
  val dynamic = Opts.flag("dynamic-plugins", "resolve plugins from the normal classpath", "d").orFalse
  val plugins = Opts.options[URL]("plugin-folder", "folder with IDML plugin jars", "pf").orNone
  val functionResolver: Opts[FunctionResolverService] = (dynamic, plugins).mapN { (d, pf) =>
    val baseFunctionResolver = if (d) { new FunctionResolverService } else {
      new StaticFunctionResolverService(
        (StaticFunctionResolverService.defaults.asScala ++ List(new JsoupFunctionResolver(), new HashingFunctionResolver())).asJava)
    }
    pf.fold(baseFunctionResolver) { urls =>
      FunctionResolverService.orElse(baseFunctionResolver, new PluginFunctionResolverService(urls.toList.toArray))
    }
  }

  val repl = Command(
    name = "repl",
    header = "IDML REPL"
  ) {
    functionResolver.map { fr =>
      IO {
        new Repl().runInner(List().toArray, Some(fr))
        ExitCode.Success
      }
    }
  }

  def server(implicit c: ConcurrentEffect[IO], t: Timer[IO]) =
    Command(
      name = "server",
      header = "IDML language server"
    ) {
      val bindAll = Opts.flag("bind-all", "Bind to all interfaces", short = "b").orFalse
      (bindAll, functionResolver).mapN {
        case (b, fr) =>
          BlazeBuilder[IO]
            .mountService(WebsocketServer.service(fr), "/")
            .bindHttp(8081, if (b) "0.0.0.0" else "localhost")
            .serve
            .compile
            .lastOrError
      }
    }

  def apply(implicit cs: ContextShift[IO]) =
    Command(
      name = "apply",
      header = "IDML command line tool",
    ) {
      val pretty   = Opts.flag("pretty", "Enable pretty printing of output", short = "p").orFalse
      val unmapped = Opts.flag("unmapped", "This probably doesn't do what you think it does", short = "u").orFalse
      val strict   = Opts.flag("strict", "Enable strict mode", short = "s").orFalse
      val file     = Opts.arguments[File]("mapping file").orEmpty
      val traced   = Opts.flag("traced", "Enable tracing mode", short = "t").orFalse

      val log = LoggerFactory.getLogger("idml-tool")

      (pretty, unmapped, strict, file, functionResolver, traced)
        .mapN { (p, u, s, f, fr, traced) =>
          val config = new IdmlToolConfig(f, p, s, u)

          val listeners = if (config.unmapped) List[PtolemyListener](new UnmappedFieldsFinder) else List.empty
          val ptolemy = new Ptolemy(
            new PtolemyConf,
            listeners.asJava,
            fr
          )

          for {
            files <- EitherT(IO {
                      val (found, missing) = config.files.partition(_.exists())
                      NonEmptyList.fromList(missing) match {
                        case Some(missingnel) =>
                          missingnel.map(f => s"Couldn't load mapping from $f").asLeft[List[File]]
                        case None =>
                          found.asRight[NonEmptyList[String]]
                      }
                    })
            _ <- EitherT.cond[IO](!(traced && files.size > 1),(),  NonEmptyList.of("You can only trace with a single mapping"))
            bodies <- files
                       .traverse { f =>
                         fs2.io.file
                           .readAll[IO](f.toPath, global, 2048)
                           .through(fs2.text.utf8Decode[IO])
                           .compile
                           .foldMonoid
                       }
                       .attemptT
                       .leftMap(e => NonEmptyList.of(e.getLocalizedMessage))
            compiled <- bodies
                         .traverse { s =>
                           IO { ptolemy.fromString(s) }
                         }
                         .attemptT
                         .leftMap(e => NonEmptyList.of(e.getLocalizedMessage))
            chain = ptolemy.newChain(compiled: _*)
            _ <- compiled
                  .traverse { c =>
                    config.strict
                      .pure[IO]
                      .ifM(
                        IO { DocumentValidator.validate(c.nodes) },
                        IO.unit
                      )
                  }
                  .attemptT
                  .leftMap(e => NonEmptyList.of(e.getLocalizedMessage))
            _ <- traced
                  .pure[IO]
                  .ifM(
                    for {
                      input <- fs2.io
                                .stdin[IO](2048, global)
                                .through(fs2.text.utf8Decode[IO])
                                .compile
                                .foldMonoid
                      json <- IO {
                               PtolemyJson.parse(input)
                             }
                      out <- IO {
                              val a   = new Annotator
                              val out = PtolemyJson.newObject()
                              val ctx = new PtolemyContext(json, out, List[PtolemyListener](a))
                              compiled.head.run(ctx)
                              a.render(bodies.head)
                            }
                      _ <- IO {
                        println(out)
                      }
                    } yield (),
                    fs2.io
                      .stdin[IO](2048, global)
                      .through(fs2.text.utf8Decode[IO])
                      .through(fs2.text.lines[IO])
                      .evalTap { line =>
                        IO {
                          chain.run(PtolemyJson.parse(line))
                        }.attemptT
                          .semiflatMap { out =>
                            config.pretty
                              .pure[IO]
                              .ifM(
                                IO { println(PtolemyJson.pretty(out)) },
                                IO { println(PtolemyJson.compact(out)) }
                              )
                          }
                          .leftSemiflatMap { err =>
                            IO { System.err.println(err.getLocalizedMessage) }
                          }
                          .merge
                      }
                      .compile
                      .drain
                  )
                  .attemptT
                  .leftMap(e => NonEmptyList.of(e.getLocalizedMessage))
          } yield ()
        }
        .map(_.biSemiflatMap(
          errors => errors.traverse(e => IO { System.err.println(e) }).as(ExitCode.Error),
          _ => ExitCode.Success.pure[IO]
        ).merge)
    }
}
