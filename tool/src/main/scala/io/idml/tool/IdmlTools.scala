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
      new Repl().runInner(List().toArray, Some(fr))
    }
  }

  val server = Command(
    name = "server",
    header = "IDML language server"
  ) {
    val bindAll = Opts.flag("bind-all", "Bind to all interfaces", short = "b").orFalse
    (bindAll, functionResolver).mapN {
      case (b, fr) =>
        BlazeBuilder[IO]
          .mountService(WebsocketServer.service(fr))
          .bindHttp(8081, if (b) "0.0.0.0" else "localhost")
          .serve
          .compile
          .drain
          .unsafeRunSync
    }
  }

  val apply = Command(
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
          missing.foreach { f =>
            println("Couldn't load mapping from %s".format(f))
          }
          sys.exit(1)
        case true =>
          val maps  = found.map(f => ptolemy.fromFile(f.getAbsolutePath))
          val chain = ptolemy.newChain(maps: _*)
          if (config.strict) {
            maps.foreach { m =>
              DocumentValidator.validate(m.nodes)
            }
          }
          scala.io.Source.stdin
            .getLines()
            .filter(!_.isEmpty)
            .map { s: String =>
              Try {
                chain.run(PtolemyJson.parse(s))
              }
            }
            .foreach {
              case Success(json) =>
                config.pretty match {
                  case true  => println(PtolemyJson.pretty(json))
                  case false => println(PtolemyJson.compact(json))
                }
                Console.flush()
              case Failure(e) =>
                log.error("Unable to process input", e)
            }
      }
    }
  }
}
