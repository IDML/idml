import java.io.File
import java.net.URI

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

  val repl = Command(
    name = "repl",
    header = "IDML REPL"
  ) {
    Opts.unit.map { _ =>
      new Repl().run(List().toArray)
    }
  }

  val server = Command(
    name = "server",
    header = "IDML language server"
  ) {
    val bindAll = Opts.flag("bind-all", "Bind to all interfaces", short = "b").orFalse
    bindAll.map { b =>
      BlazeBuilder[IO]
        .mountService(WebsocketServer.service)
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
    val trace    = Opts.option[File]("tracefile", "tracing output file").orNone
    val file     = Opts.arguments[File]("mapping file").orEmpty

    val log = LoggerFactory.getLogger("idml-tool")

    (pretty, unmapped, strict, trace, file).mapN { (p, u, s, t, f) =>
      val config = new IdmlToolConfig(f, t, p, s, u)
      val annotator = t.map(_ => new Annotator)

      val ptolemy =
        new Ptolemy(
          new PtolemyConf,
          List[Option[PtolemyListener]](
            if (config.unmapped) Some(new UnmappedFieldsFinder) else None,
            annotator
          ).flatten.asJava,
          new StaticFunctionResolverService(
            (StaticFunctionResolverService.defaults.asScala ++ List(new JsoupFunctionResolver(),
                                                                    new HashingFunctionResolver())).asJava)
        )

      val (found, missing) = config.files.partition(_.exists())
      missing.isEmpty match {
        case false =>
          missing.foreach { f =>
            println("Couldn't load mapping from %s".format(f))
          }
          sys.exit(1)
        case true =>
          val strings = found.map(f => scala.io.Source.fromFile(f).mkString)
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
                (t, annotator).tupled.foreach { case (traceFile, annotatorModule) =>
                  if (maps.size == 1) {
                    reflect.io.File(traceFile).writeAll(annotatorModule.render(strings.head))
                  }
                }
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
