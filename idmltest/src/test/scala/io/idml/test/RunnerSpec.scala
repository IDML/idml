package io.idml.test

import java.nio.file.{Files, Paths}

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
    state must equal(List(TestState.Success, TestState.Success))
    r.printed.toList must equal(
      List(
        fansi.Color.Green("basic test unchanged").toString(),
        fansi.Color.Green("basic.json unchanged, not flushing file").toString()
      )
    )
  }
  "be able to update a file that needs updating" in {
    val test     = Files.createTempFile("idml-test", ".json")
    val r        = new TestRunner
    val testJson = json"""
      {
        "name" : "example test",
        "code" : "r = a + b",
        "input" : {
           "a" : 2,
           "b" : 2
        },
        "output" : {
        }
      }"""
    r.writeAll(test)(fs2.Stream.emit(testJson.spaces2)).unsafeRunSync()
    val state = r.updateTest(false)(test).unsafeRunSync()
    Files.delete(test)
    state must equal(List(TestState.Updated, TestState.Updated))
    r.printed.toList must equal(
      List(
        fansi.Color.Cyan("example test updated inline").toString(),
        fansi.Color.Cyan(s"flushing update to ${test.getFileName}").toString()
      )
    )
  }
  "be able to update a referred file that needs updating" in {
    val r = new TestRunner

    {
      for {
        test   <- IO { Files.createTempFile("idml-test", ".json") }
        output <- IO { Files.createTempFile("idml-test", ".json") }
        ref    = "$ref"
        testJson = Json.obj(
          "name" -> Json.fromString("example test"),
          "code" -> Json.fromString("r = a + b"),
          "input" -> Json.obj(
            "a" -> Json.fromInt(2),
            "b" -> Json.fromInt(2)
          ),
          "output" -> Json.obj(
            "$ref" -> Json.fromString(output.getFileName.toString)
          )
        )
        _     <- r.writeAll(test)(fs2.Stream.emit(testJson.spaces2))
        _     <- r.writeAll(output)(fs2.Stream.emit("{}"))
        state <- r.updateTest(false)(test)
        _     <- IO { Files.delete(test) }
        _     <- IO { Files.delete(output) }
      } yield {
        state must equal(List(TestState.Success, TestState.Updated))
        r.printed.toList must equal(
          List(
            fansi.Color.Cyan("example test updated").toString(),
            fansi.Color.Green(s"${test.getFileName} unchanged, not flushing file").toString()
          )
        )
      }
    }.unsafeRunSync()
  }
}
