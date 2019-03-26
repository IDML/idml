package io.idml.test
import java.nio.file.Path

import cats._
import cats.data._
import cats.implicits._
import cats.effect._
import com.monovore.decline._

object Main extends IOApp {
  val arg      = Opts.arguments[Path]("test.json")
  val filter   = Opts.option[String]("filter", "filter to run specific tests, use re2 compatible regex or a prefix", "f").orNone
  val update   = Opts.flag("update", "update test snapshots", "u").orFalse
  val diff     = Opts.flag("json-diff", "output JSON diffs in stead", "j").orFalse
  val failures = Opts.flag("failed-only", "suppress successful output", "x")
  // flag to suppress successful output?

  // use re2 for the regex filtering

  val command = Command("test", "run IDML tests")((arg, filter, update).tupled)

  override def run(args: List[String]): IO[ExitCode] = execute.parse(args) match {
    case Left(h) =>
      IO {
        println(h)
        ExitCode.Error
      }
    case Right(r) => r
  }

  val execute = command.map {
    case (paths, filter, update) =>
      for {
        compiled <- if (update) paths.traverse(Runner.updateTest) else paths.traverse(Runner.runTest)
      } yield ExitCode.Success
  }
}
