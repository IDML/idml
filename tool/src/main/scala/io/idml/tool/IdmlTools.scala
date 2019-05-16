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
import io.circe._

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

      val log = LoggerFactory.getLogger("idml-tool")

      (pretty, unmapped, strict, file, functionResolver).mapN { (p, u, s, f, fr) =>
        val config = new IdmlToolConfig(f, p, s, u)

        val ptolemy = if (config.unmapped) {
          new Ptolemy(
            new PtolemyConf,
            List[PtolemyListener](new UnmappedFieldsFinder).asJava,
            fr
          )
        } else {
          new Ptolemy(
            new PtolemyConf,
            fr
          )
        }
        val (found, missing) = config.files.partition(_.exists())
        missing.isEmpty match {
          case false =>
            missing.traverse { f =>
              IO {
                s"Couldn't load mapping from ${f.toString}"
              }
            } *> IO.pure(ExitCode.Error)
          case true =>
            for {
              maps  <- found.traverse(f => IO { ptolemy.fromFile(f.getAbsolutePath) })
              chain = ptolemy.newChain(maps: _*)
              _ <- OptionT
                    .fromOption[IO](Option(()).filter(_ => config.strict))
                    .semiflatMap(_ => maps.traverse(m => IO { DocumentValidator.validate(m.nodes) }))
                    .value
              _ <- fs2.io
                    .stdin[IO](1024, global)
                    .through(fs2.text.utf8Decode[IO])
                    .through(fs2.text.lines[IO])
                    .filter(_.nonEmpty)
                    .evalMap { line =>
                      IO {
                        chain.run(PtolemyJson.parse(line))
                      }.attemptT
                        .map { result =>
                          if (config.pretty) {
                            PtolemyJson.pretty(result)
                          } else {
                            PtolemyJson.compact(result)
                          }
                        }
                        .leftMap { exception =>
                          val output = Json.obj(
                            "error" -> Json.fromString(exception.getLocalizedMessage),
                            "input" -> Json.fromString(line)
                          )
                          if (config.pretty) {
                            output.spaces2
                          } else {
                            output.noSpaces
                          }
                        }
                        .value
                    }
                    .evalTap(
                      s =>
                        s.bitraverse(
                            l => IO { System.err.println(l) },
                            r => IO { System.out.println(r) }
                          )
                          .void)
                    .compile
                    .drain
            } yield ExitCode.Success
        }
      }
    }
}
