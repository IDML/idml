package io.idml.test
import java.nio.file.Path

import cats._
import cats.data._
import cats.implicits._
import cats.effect._
import com.monovore.decline._

object Main extends IOApp {

  val arg = Opts.arguments[Path]("test.json")
  val filter = Opts.option[String]("filter", "filter to run specific tests", "f").orNone
  val update = Opts.flag("update", "update test snapshots", "u").orFalse

  val command = Command("test", "run IDML tests")((arg, filter, update).tupled)

  override def run(args: List[String]): IO[ExitCode] = command.parse(args) match {
    case Left(h) => IO { println(h); ExitCode.Error }
    case Right((paths, filter, update)) =>
      IO {
        println((paths, filter, update))
        ExitCode.Success
      }
  }
}
