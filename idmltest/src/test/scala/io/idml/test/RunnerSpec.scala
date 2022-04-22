package io.idml.test

import java.nio.file.{Files, Paths}
import cats.effect.{Blocker, IO, Timer}
import com.google.re2j.Pattern
import io.circe.Json
import io.circe.generic.auto._
import io.circe.literal.JsonStringContext
import io.circe.syntax._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must
import io.circe.literal._
import org.scalatest.BeforeAndAfterAll

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

class RunnerSpec
    extends AnyWordSpec
    with must.Matchers
    with CirceEitherEncoders
    with BeforeAndAfterAll {

  implicit val cs                  = IO.contextShift(global)
  val (blocker, deallocateBlocker) = Blocker[IO].allocated.unsafeRunSync()

  override def afterAll(): Unit = deallocateBlocker.unsafeRunSync()

  class TestRunner extends Runner(false, None, true, blocker)(cs) {
    val printed                          = mutable.Buffer.empty[String]
    override def print(a: Any): IO[Unit] = IO { printed.append(a.toString) }
  }

  "Runner" should {
    "be able to run IDML" in {
      val r = new TestRunner
      r.runSingle(
        None,
        Left("r = a + b"),
        Json.obj("a" -> Json.fromInt(1), "b" -> Json.fromInt(2))
      ).unsafeRunSync() must equal(Json.obj("r" -> Json.fromInt(3)))
    }
  }
  "be able to run IDML with an injected time" in {
    val r = new TestRunner
    r.runSingle(
      Some(1554117846L),
      Left("r = now()"),
      Json.obj()
    ).unsafeRunSync() must equal(
      Json.obj("r" -> Json.fromString("Mon, 01 Apr 2019 11:24:06 +0000")))
  }
  "be able to run a test" in {
    val test  = Paths.get(getClass.getResource("/tests/basic.json").getFile)
    val r     = new TestRunner
    val state = r.runTest(false)(cs)(test).unsafeRunSync()
    state must equal(List(TestState.Success))
    r.printed.toList must equal(
      List(fansi.Color.Green("basic test passed").toString())
    )
  }
  "be able to run a pipeline test" in {
    val test  = Paths.get(getClass.getResource("/tests/basic-pipeline.json").getFile)
    val r     = new TestRunner
    val state = r.runTest(false)(cs)(test).unsafeRunSync()
    state must equal(List(TestState.Success))
    r.printed.toList must equal(
      List(fansi.Color.Green("basic pipeline test passed").toString())
    )
  }
  "be able to run a pipeline test with references" in {
    val test  = Paths.get(getClass.getResource("/tests/pipeline-ref.json").getFile)
    val r     = new TestRunner
    val state = r.runTest(false)(cs)(test).unsafeRunSync()
    state must equal(List(TestState.Success))
    r.printed.toList must equal(
      List(fansi.Color.Green("pipeline reference test passed").toString())
    )
  }
  "be able to run a test with nulls" in {
    val test  = Paths.get(getClass.getResource("/tests/null-behaviour.json").getFile)
    val r     = new TestRunner
    val state = r.runTest(false)(cs)(test).unsafeRunSync()
    state must equal(List(TestState.Success))
    r.printed.toList must equal(
      List(fansi.Color.Green("null behaviour passed").toString())
    )
  }
  "be able to run a multitest" in {
    val test  = Paths.get(getClass.getResource("/tests/basic-multitest.json").getFile)
    val r     = new TestRunner
    val state = r.runTest(false)(cs)(test).unsafeRunSync()
    state must equal(List(TestState.Success, TestState.Success))
    r.printed.toList must equal(
      List(
        fansi.Color.Green("basic multitest #1 passed").toString(),
        fansi.Color.Green("basic multitest #2 passed").toString()
      )
    )
  }
  "be able to run a test with an injected time" in {
    val test  = Paths.get(getClass.getResource("/tests/inject-now.json").getFile)
    val r     = new TestRunner
    val state = r.runTest(false)(cs)(test).unsafeRunSync()
    state must equal(List(TestState.Success))
    r.printed.toList must equal(
      List(fansi.Color.Green("injected now test passed").toString())
    )
  }
  "be able to run a test with a ref" in {
    val test  = Paths.get(getClass.getResource("/tests/basic-ref.json").getFile)
    val r     = new TestRunner
    val state = r.runTest(false)(cs)(test).unsafeRunSync()
    state must equal(List(TestState.Success))
    r.printed.toList must equal(
      List(fansi.Color.Green("basic test with a ref passed").toString())
    )
  }
  "be able to run a test which fails" in {
    val test  = Paths.get(getClass.getResource("/tests/basic-failed.json").getFile)
    val r     = new TestRunner
    val state = r.runTest(false)(cs)(test).unsafeRunSync()
    state must equal(List(TestState.Failed))
  }
  "be able to run a test which has an invalid reference" in {
    val test  = Paths.get(getClass.getResource("/tests/basic-invalid-ref.json").getFile)
    val r     = new TestRunner
    val state = r.runTest(false)(cs)(test).unsafeRunSync()
    state must equal(List(TestState.Error))
  }
  "be able to print out a report" in {
    val r = new TestRunner
    r.report(List(TestState.Success, TestState.Success, TestState.Failed, TestState.Error))
      .unsafeRunSync()
    r.printed.toList must equal(
      List(
        "---",
        "Test Summary:",
        fansi.Color.Green("2 tests succeeded").toString(),
        fansi.Color.Red("1 test failed").toString(),
        fansi.Color.Red("1 test errored").toString()
      )
    )
  }
  "only print out things that happened in the report" in {
    val r = new TestRunner
    r.report(List(TestState.Updated)).unsafeRunSync()
    r.printed.toList must equal(
      List(
        "---",
        "Test Summary:",
        fansi.Color.Cyan("1 test updated").toString()
      )
    )
  }
  "be able to tell when a test doesn't need updating" in {
    val test  = Paths.get(getClass.getResource("/tests/basic.json").getFile)
    val r     = new TestRunner
    val state = r.updateTest(false)(cs)(test).unsafeRunSync()
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
    val testJson =
      Json.obj(
        "name"   -> Json.fromString("example test"),
        "code"   -> Json.fromString("r = a + b"),
        "input"  -> Json.obj(
          "a" -> Json.fromInt(2),
          "b" -> Json.fromInt(2)
        ),
        "output" -> Json.obj()
      )
    Blocker[IO]
      .use { global =>
        r.writeAll(global)(test)(fs2.Stream.emit(testJson.spaces2))
      }
      .unsafeRunSync()
    val state    = r.updateTest(false)(cs)(test).unsafeRunSync()
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

    Blocker[IO]
      .use { global =>
        for {
          test    <- IO { Files.createTempFile("idml-test", ".json") }
          output  <- IO { Files.createTempFile("idml-test", ".json") }
          ref      = "$ref"
          testJson = Json.obj(
                       "name"   -> Json.fromString("example test"),
                       "code"   -> Json.fromString("r = a + b"),
                       "input"  -> Json.obj(
                         "a" -> Json.fromInt(2),
                         "b" -> Json.fromInt(2)
                       ),
                       "output" -> Json.obj(
                         "$ref" -> Json.fromString(output.getFileName.toString)
                       )
                     )
          _       <- r.writeAll(global)(test)(fs2.Stream.emit(testJson.spaces2))
          _       <- r.writeAll(global)(output)(fs2.Stream.emit("{}"))
          state   <- r.updateTest(false)(cs)(test)
          _       <- IO { Files.delete(test) }
          updated <- r.readAll(global)(output).flatMap(r.parseJ)
          _       <- IO { Files.delete(output) }
        } yield {
          state must equal(List(TestState.Success, TestState.Updated))
          r.printed.toList must equal(
            List(
              fansi.Color.Cyan("example test updated").toString(),
              fansi.Color.Green(s"${test.getFileName} unchanged, not flushing file").toString()
            )
          )
          updated must equal(Json.obj("r" -> Json.fromInt(4)))
        }
      }
      .unsafeRunSync()
  }
  "be able to create a referred file that needs creating" in {
    val r = new TestRunner

    Blocker[IO]
      .use { global =>
        for {
          test    <- IO { Files.createTempFile("idml-test", ".json") }
          output   = Paths.get(test.getParent.toString, "create-me.json")
          testJson = Json.obj(
                       "name"   -> Json.fromString("creation test"),
                       "code"   -> Json.fromString("r = a + b"),
                       "input"  -> Json.obj(
                         "a" -> Json.fromInt(2),
                         "b" -> Json.fromInt(2)
                       ),
                       "output" -> Json.obj(
                         "$ref" -> Json.fromString(output.toAbsolutePath.toString)
                       )
                     )
          _       <- r.writeAll(global)(test)(fs2.Stream.emit(testJson.spaces2))
          state   <- r.updateTest(false)(cs)(test)
          _       <- IO { Files.delete(test) }
          updated <- r.readAll(global)(output).flatMap(r.parseJ)
          _       <- IO { Files.delete(output) }
        } yield {
          state must equal(List(TestState.Success, TestState.Updated))
          r.printed.toList must equal(
            List(
              fansi.Color.Cyan("creation test updated").toString(),
              fansi.Color.Green(s"${test.getFileName} unchanged, not flushing file").toString()
            )
          )
          updated must equal(Json.obj("r" -> Json.fromInt(4)))
        }
      }
      .unsafeRunSync()
  }
  "be able to filter tests in run" in {
    val test  = Paths.get(getClass.getResource("/tests/two-tests.json").getFile)
    val r     = new TestRunner
    val state = r.runTest(false, Some(Pattern.compile(".*aaa")))(cs)(test).unsafeRunSync()
    state must equal(List(TestState.Success))
    r.printed.toList must equal(
      List(fansi.Color.Green("basic test aaa passed").toString())
    )
  }
  "be able to filter tests in update" in {
    val test  = Paths.get(getClass.getResource("/tests/two-tests.json").getFile)
    val r     = new TestRunner
    val state = r.updateTest(false, Some(Pattern.compile(".*aaa")))(cs)(test).unsafeRunSync()
    state must equal(List(TestState.Success, TestState.Success))
    r.printed.toList must equal(
      List(
        fansi.Color.Green("basic test aaa unchanged").toString(),
        fansi.Color.Green(s"${test.getFileName} unchanged, not flushing file").toString()
      )
    )
  }
  "be able to tell when a test with an injected time doesn't need updating" in {
    val test  = Paths.get(getClass.getResource("/tests/inject-now.json").getFile)
    val r     = new TestRunner
    val state = r.updateTest(false)(cs)(test).unsafeRunSync()
    state must equal(List(TestState.Success, TestState.Success))
    r.printed.toList must equal(
      List(
        fansi.Color.Green("injected now test unchanged").toString(),
        fansi.Color.Green("inject-now.json unchanged, not flushing file").toString()
      )
    )
  }
  "be able to fail validation of a multitest with mismatched arrays" in {
    val test  = Paths.get(getClass.getResource("/tests/bad-multitest.json").getFile)
    val r     = new TestRunner
    val state = r.runTest(false)(cs)(test).unsafeRunSync()
    state must equal(List(TestState.Error))
    r.printed.toList must equal(
      List(
        fansi.Color.Red("bad-multitest.json errored when loading").toString(),
        fansi.Color
          .Red("io.idml.test.UnbalancedMultiTest: bad multitest must have the same number of inputs and outputs")
          .toString()
      )
    )
  }
  "be able to update an inline multitest" in {
    val r = new TestRunner

    Blocker[IO]
      .use { global =>
        for {
          test    <- IO { Files.createTempFile("idml-test", ".json") }
          testJson = Json.obj(
                       "name"   -> Json.fromString("multitest update test"),
                       "code"   -> Json.fromString("r = a + b"),
                       "input"  -> Json.arr(
                         Json.obj(
                           "a" -> Json.fromInt(2),
                           "b" -> Json.fromInt(2)
                         )
                       ),
                       "output" -> Json.arr()
                     )
          _       <- r.writeAll(global)(test)(fs2.Stream.emit(testJson.spaces2))
          state   <- r.updateTest(false)(cs)(test)
          updated <- r.readAll(global)(test).flatMap(r.parseJ)
          _       <- IO { Files.delete(test) }
        } yield {
          state must equal(List(TestState.Updated, TestState.Updated))
          r.printed.toList must equal(
            List(
              fansi.Color.Cyan("multitest update test updated inline").toString(),
              fansi.Color.Cyan(s"flushing update to ${test.getFileName}").toString()
            )
          )
          updated.hcursor.downField("output").focus must equal(
            Some(Json.arr(Json.obj("r" -> Json.fromInt(4)))))
        }
      }
      .unsafeRunSync()
  }
  "be able to update a referred file that needs updating in a multitest" in {
    val r = new TestRunner

    Blocker[IO]
      .use { global =>
        for {
          test    <- IO { Files.createTempFile("idml-test", ".json") }
          output  <- IO { Files.createTempFile("idml-test", ".json") }
          ref      = "$ref"
          testJson = Json.obj(
                       "name"   -> Json.fromString("example multitest"),
                       "code"   -> Json.fromString("r = a + b"),
                       "input"  -> Json.arr(
                         Json.obj(
                           "a" -> Json.fromInt(2),
                           "b" -> Json.fromInt(2)
                         )
                       ),
                       "output" -> Json.obj(
                         "$ref" -> Json.fromString(output.getFileName.toString)
                       )
                     )
          _       <- r.writeAll(global)(test)(fs2.Stream.emit(testJson.spaces2))
          _       <- r.writeAll(global)(output)(fs2.Stream.emit("[]"))
          state   <- r.updateTest(false)(cs)(test)
          _       <- IO { Files.delete(test) }
          updated <- r.readAll(global)(output).flatMap(r.parseJ)
          _       <- IO { Files.delete(output) }
        } yield {
          state must equal(List(TestState.Success, TestState.Updated))
          r.printed.toList must equal(
            List(
              fansi.Color.Cyan("example multitest updated").toString(),
              fansi.Color.Green(s"${test.getFileName} unchanged, not flushing file").toString()
            )
          )
          updated must equal(Json.arr(Json.obj("r" -> Json.fromInt(4))))
        }
      }
      .unsafeRunSync()
  }
}
