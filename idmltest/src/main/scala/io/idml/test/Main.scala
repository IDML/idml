package io.idml.test
import java.net.URL
import java.nio.file.Path

import cats._
import cats.data._
import cats.implicits._
import cats.effect._
import com.google.re2j.Pattern
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
  implicit val patternArgument = new Argument[Pattern] {
    override def read(string: String): ValidatedNel[String, Pattern] =
      Try {
        Pattern.compile(string)
      }.toEither.leftMap(_.getMessage).toValidatedNel
    override def defaultMetavar: String = "pattern"
  }

  val arg      = Opts.arguments[Path]("test.json")
  val filter   = Opts.option[Pattern]("filter", "filter to run specific tests, use re2 compatible regex or a prefix", "f").orNone
  val update   = Opts.flag("update", "update test snapshots", "u").orFalse
  val diff     = Opts.flag("json-diff", "output JSON diffs in stead", "j").orFalse
  val failures = Opts.flag("failed-only", "suppress successful output", "x").orFalse
  val dynamic  = Opts.flag("dynamic-plugins", "resolve plugins from the normal classpath", "d").orFalse
  val plugins  = Opts.options[URL]("plugin-folder", "folder with IDML plugin jars", "pf").orNone
  val noReport = Opts.flag("no-report", "disable the test reporter at the end of a run", "nr").orFalse

  val command = Command("test", "run IDML tests")((arg, filter, update, failures, dynamic, plugins, noReport).tupled)

  override def run(args: List[String]): IO[ExitCode] = execute().parse(args) match {
    case Left(h) =>
      IO {
        println(h)
        ExitCode.Error
      }
    case Right(r) => r
  }

  def execute(injectedRunner: Option[Runner] = None): Command[IO[ExitCode]] = command.map {
    case (paths, filter, update, failures, dynamic, plugins, noReport) =>
      val runner = injectedRunner.getOrElse(new Runner(dynamic, plugins))
      for {
        results  <- if (update) paths.traverse(runner.updateTest(failures, filter)) else paths.traverse(runner.runTest(failures, filter))
        results2 = results.toList.flatten
        _        <- noReport.pure[IO].ifM(IO.unit, runner.report(results2))
      } yield TestState.toExitCode(results2.combineAll)
  }
}
