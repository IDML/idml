package io.idml.test

import java.nio.file.Paths

import cats.effect.{ExitCode, IO}
import io.circe.Json
import io.circe.generic.auto._
import io.circe.literal.JsonStringContext
import io.circe.syntax._
import org.scalatest.{MustMatchers, WordSpec}

import scala.collection.mutable

class MainSpec extends WordSpec with MustMatchers with CirceEitherEncoders {
  class TestRunner extends Runner(false, None) {
    val printed                          = mutable.Buffer.empty[String]
    override def print(a: Any): IO[Unit] = IO { printed.append(a.toString) }
  }
  "Main" should {
    "run a real test and translate it's exit code" in {
      val r    = new TestRunner
      val test = Paths.get(getClass.getResource("/tests/basic.json").getFile)
      Main.execute(Some(r)).parse(List(test.toAbsolutePath.toString)).right.get.unsafeRunSync() must equal(ExitCode.Success)
      r.printed must equal(
        List(
          fansi.Color.Green("basic test passed").toString(),
          "---",
          "Test Summary:",
          fansi.Color.Green("1 test succeeded").toString()
        )
      )
    }
  }
}
