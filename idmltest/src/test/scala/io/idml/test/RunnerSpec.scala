package io.idml.test

import java.nio.file.Paths

import cats.effect.IO
import io.circe.Json
import io.circe.generic.auto._
import io.circe.literal.JsonStringContext
import io.circe.syntax._
import org.scalatest.{MustMatchers, WordSpec}
import io.circe.literal._

import scala.collection.mutable

class RunnerSpec extends WordSpec with MustMatchers with CirceEitherEncoders {

  class TestRunner extends Runner(false, None) {
    val printed                          = mutable.Buffer.empty[String]
    override def print(a: Any): IO[Unit] = IO { printed.append(a.toString) }
  }

  "Runner" should {
    "be able to run IDML" in {
      val r = new TestRunner
      r.run(
          "r = a + b",
          json"""
               {
                 "a": 1,
                 "b": 2
               }
        """
        )
        .unsafeRunSync() must equal(Json.obj("r" -> Json.fromInt(3)))
    }
  }
  "be able to run a test" in {
    val test  = Paths.get(getClass.getResource("/tests/basic.json").getFile)
    val r     = new TestRunner
    val state = r.runTest(false)(test).unsafeRunSync()
    state must equal(List(TestState.Success))
    r.printed.toList must equal(
      List(fansi.Color.Green("basic test passed").toString())
    )
  }
  "be able to run a test with a ref" in {
    val test  = Paths.get(getClass.getResource("/tests/basic-ref.json").getFile)
    val r     = new TestRunner
    val state = r.runTest(false)(test).unsafeRunSync()
    state must equal(List(TestState.Success))
    r.printed.toList must equal(
      List(fansi.Color.Green("basic test with a ref passed").toString())
    )
  }
  "be able to run a test which fails" in {
    val test  = Paths.get(getClass.getResource("/tests/basic-failed.json").getFile)
    val r     = new TestRunner
    val state = r.runTest(false)(test).unsafeRunSync()
    state must equal(List(TestState.Failed))

  }
  "be able to run a test which has an invalid reference" in {
    val test  = Paths.get(getClass.getResource("/tests/basic-invalid-ref.json").getFile)
    val r     = new TestRunner
    val state = r.runTest(false)(test).unsafeRunSync()
    state must equal(List(TestState.Error))
  }
  "be able to print out a report" in {
    val r = new TestRunner
    r.report(List(TestState.Success, TestState.Success, TestState.Failed, TestState.Error)).unsafeRunSync()
    r.printed.toList must equal(
      List(
        "---",
        "Test Summary:",
        fansi.Color.Red("1 test failed").toString(),
        fansi.Color.Green("2 tests succeeded").toString(),
        fansi.Color.Red("1 test errored").toString()
      ))
  }
  "only print out things that happened in the report" in {
    val r = new TestRunner
    r.report(List(TestState.Updated)).unsafeRunSync()
    r.printed.toList must equal(
      List(
        "---",
        "Test Summary:",
        fansi.Color.Cyan("1 test updated").toString(),
      ))
  }
  "be able to tell when a test doesn't need updating" in {
    val test  = Paths.get(getClass.getResource("/tests/basic.json").getFile)
    val r     = new TestRunner
    val state = r.updateTest(false)(test).unsafeRunSync()
    state must equal(List(TestState.Success))
    r.printed.toList must equal(
      List(
        fansi.Color.Green("basic test unchanged").toString(),
        fansi.Color.Green("basic.json unchanged, not flushing file").toString()
      )
    )
  }
}
