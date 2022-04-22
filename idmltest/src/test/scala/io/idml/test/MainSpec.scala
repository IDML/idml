package io.idml.test

import cats.implicits._
import java.nio.file.Paths
import cats.effect.{Blocker, ExitCode, IO, Resource}
import io.circe.Json
import io.circe.generic.auto._
import io.circe.literal.JsonStringContext
import io.circe.syntax._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must

import scala.collection.mutable
import scala.concurrent.ExecutionContext.global

class MainSpec extends AnyWordSpec with must.Matchers with CirceEitherEncoders {
  implicit val cs = IO.contextShift(global)
  class TestRunner(b: Blocker) extends Runner(false, None, true, b) {
    val printed                          = mutable.Buffer.empty[String]
    override def print(a: Any): IO[Unit] = IO { printed.append(a.toString) }
  }
  val runner: Resource[IO, TestRunner] = Blocker[IO].map { b => new TestRunner(b) }
  "Main" should {
    "run a real test and translate it's exit code" in {
      val test = Paths.get(getClass.getResource("/tests/basic.json").getFile)
      runner
        .use { r =>
          Main
            .execute(Some(r))
            .parse(List(test.toAbsolutePath.toString))
            .right
            .toOption
            .get
            .flatMap { code =>
              IO { (code, r.printed.toList) }
            }
        }
        .unsafeRunSync() must equal(
        (
          ExitCode.Success,
          List(
            fansi.Color.Green("basic test passed").toString(),
            "---",
            "Test Summary:",
            fansi.Color.Green("1 test succeeded").toString()
          )
        )
      )
    }
    "run a real test which fails and translate it's exit code" in {
      val test = Paths.get(getClass.getResource("/tests/basic-failed.json").getFile)
      runner
        .use { r =>
          Main
            .execute(Some(r))
            .parse(List(test.toAbsolutePath.toString))
            .right
            .toOption
            .get
            .flatMap { code =>
              IO { (code, r.printed.toList) }
            }
        }
        .unsafeRunSync() must equal(
        (
          ExitCode.Error,
          List(
            fansi.Color.Red("basic test output differs").toString(),
            """[
  {
    "op" : "replace",
    "path" : "/r",
    "value" : 4
  }
]""",
            "---",
            "Test Summary:",
            fansi.Color.Red("1 test failed").toString()
          )
        )
      )
    }
  }
}
