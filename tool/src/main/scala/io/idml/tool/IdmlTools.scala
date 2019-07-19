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
import io.idml.utils.{ClassificationException, DocumentValidator}
import io.idmlrepl.Repl
import io.idml.hashing.HashingFunctionResolver
import io.idml.jackson.IdmlJackson
import io.idml.jsoup.JsoupFunctionResolver
import io.idml.lang.DocumentParseException
import io.idml.server.Server
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import org.http4s.server.blaze.BlazeBuilder
import io.idml.server.WebsocketServer
import io.idml.utils.Tracer.Annotator

import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source

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
        (StaticFunctionResolverService.defaults(IdmlJackson.default).asScala ++ List(new JsoupFunctionResolver(),
                                                                                     new HashingFunctionResolver())).asJava)
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
      val pretty    = Opts.flag("pretty", "Enable pretty printing of output", short = "p").orFalse
      val unmapped  = Opts.flag("unmapped", "This probably doesn't do what you think it does", short = "u").orFalse
      val strict    = Opts.flag("strict", "Enable strict mode", short = "s").orFalse
      val traceFile = Opts.option[String]("trace", "File to trace into", "t").orNone.mapValidated {
        case Some(f) => Validated.fromEither(Either.catchNonFatal(Some(new File(f))).leftMap(e => s"Invalid file: ${e.getLocalizedMessage}")).toValidatedNel
        case None => Validated.valid[String, Option[File]](None).toValidatedNel
      }
      val file      = Opts.arguments[File]("mapping file").orEmpty

      val log = LoggerFactory.getLogger("idml-tool")

      (pretty, unmapped, strict, file, functionResolver, traceFile)
        .mapN { (p, u, s, f, fr, t) =>
          val config     = new IdmlToolConfig(f, p, s, u, t)
          val jsonModule = IdmlJackson.default

          val unmappedModule = if (config.unmapped) Some(new UnmappedFieldsFinder) else None
          val analysisModule = if (config.traceFile.isDefined) Some(new Annotator(jsonModule)) else None
          val modules        = List(unmappedModule, analysisModule).flatten

          for {
            _ <- EitherT
                  .cond[IO](!(config.traceFile.isDefined && config.files.length != 1), (), "When tracing please supply one mapping to run")
            engine <- EitherT.liftF(IO {
                       new IdmlBuilder(fr)
                         .withListeners(modules: _*)
                         .build()
                     })
            found <- EitherT(IO {
                      val (exists, doesntExist) = config.files.partition(_.exists())
                      if (doesntExist.nonEmpty) {
                        doesntExist.map(f => s"Couldn't load mapping from $f").mkString("\n").asLeft
                      } else {
                        exists.asRight
                      }
                    })
            strings <- EitherT.liftF(found.traverse { f =>
                        fs2.io.file
                          .readAll[IO](f.toPath, global, 2048)
                          .through(fs2.text.utf8Decode[IO])
                          .compile
                          .foldMonoid
                      })
            compiled <- found.zip(strings).traverse {
                         case (file, s) =>
                           EitherT(IO {
                             Either
                               .catchOnly[DocumentParseException](engine.compile(s))
                               .leftMap { c =>
                                 s"Couldn't compile ${file.getName}: ${c.getMessage}"
                               }
                           })
                       }
            _ <- if (config.strict)
                  found
                    .zip(compiled)
                    .traverse {
                      case (file, m) =>
                        EitherT(IO {
                          Either
                            .catchOnly[ClassificationException] {
                              DocumentValidator.validate(m.asInstanceOf[IdmlMapping].nodes)
                            }
                            .leftMap { ce =>
                              s"Couldn't validate ${file.getName}: ${ce.getMessage}"
                            }
                        })
                    }
                    .void
                else EitherT.rightT[IO, String](())
            // and we get to the main loop!
            chain = engine.chain(compiled: _*)
            _ <- fs2.io
                  .stdin[IO](2048, global)
                  .through(fs2.text.utf8Decode[IO])
                  .through(fs2.text.lines)
                  .filter(_.nonEmpty)
                  .evalMap { line =>
                    IO.fromEither(
                      jsonModule.parseObjectEither(line).leftWiden[Throwable]
                    )
                  }
                  .evalTap { j =>
                    IO {
                      val result = engine.run(chain, j)
                      if (config.pretty) {
                        println(jsonModule.pretty(result))
                      } else {
                        println(jsonModule.compact(result))
                      }
                      result
                    }.flatMap { result =>
                      (config.traceFile, analysisModule) match {
                        case (Some(outputFile), Some(annotator)) =>
                          fs2.Stream
                            .emit(annotator.render(strings.head))
                            .covary[IO]
                            .through(fs2.text.utf8Encode[IO])
                            .through(
                              fs2.io.file.writeAll(outputFile.toPath, global)
                            )
                            .compile
                            .drain
                        case _ =>
                          IO.unit
                      }
                    }
                  }
                  .compile
                  .drain
                  .attemptT
                  .leftMap { e =>
                    s"Couldn't process input: ${e.getMessage}"
                  }
          } yield ()
        }
        .map { et =>
          et.leftSemiflatMap { error =>
              IO {
                System.err.println(error)
                ExitCode.Error
              }
            }
            .as(ExitCode.Success)
            .merge
        }
    }
}
