package io.idml.test
import java.net.URL
import java.nio.file.Path

import cats._
import cats.data._
import cats.implicits._
import cats.effect._
import com.monovore.decline._

import scala.util.Try

object Main extends IOApp {
  implicit val urlArgument = new Argument[URL] {
    override def read(string: String): ValidatedNel[String, URL] =
      Try {
        new URL(string)
      }.toEither.leftMap(_.getMessage).toValidatedNel
    override def defaultMetavar: String = "URL"
  }

  val arg      = Opts.arguments[Path]("test.json")
  val filter   = Opts.option[String]("filter", "filter to run specific tests, use re2 compatible regex or a prefix", "f").orNone
  val update   = Opts.flag("update", "update test snapshots", "u").orFalse
  val diff     = Opts.flag("json-diff", "output JSON diffs in stead", "j").orFalse
  val failures = Opts.flag("failed-only", "suppress successful output", "x").orFalse
  val dynamic  = Opts.flag("resolve-plugins", "just resolve plugins from the normal classpath", "p").orFalse
  val plugins  = Opts.options[URL]("plugin-folder", "folder with IDML plugin jars", "pf").orNone
  // use re2 for the regex filtering

  val command = Command("test", "run IDML tests")((arg, filter, update, failures, dynamic, plugins).tupled)

  override def run(args: List[String]): IO[ExitCode] = execute.parse(args) match {
    case Left(h) =>
      IO {
        println(h)
        ExitCode.Error
      }
    case Right(r) => r
  }

  val execute = command.map {
    case (paths, filter, update, failures, dynamic, plugins) =>
      val runner = new Runner(dynamic, plugins)
      for {
        results <- if (update) paths.traverse(runner.updateTest(failures)) else paths.traverse(runner.runTest(failures))
      } yield TestState.toExitCode(results.toList.flatten.combineAll)
  }
}
