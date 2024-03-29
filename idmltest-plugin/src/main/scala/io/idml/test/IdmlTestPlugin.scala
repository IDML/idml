package io.idml.test

import java.nio.file.Files

import sbt._
import sbt.Keys._
import cats.effect._
import cats._
import cats.implicits._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.global
import scala.util.control.NoStackTrace

object IdmlTestPlugin extends AutoPlugin {
  object autoImport {
    val idmlTest: TaskKey[Unit]             = taskKey[Unit]("run IDML tests")
    val idmlTestDirectory: SettingKey[File] = settingKey[File]("folder to run IDML tests in")
  }
  import autoImport._

  override val trigger = allRequirements

  class TestRunFailed(s: String) extends Throwable(s) with NoStackTrace

  override val projectSettings = Seq(
    idmlTestDirectory := (sourceDirectory in sbt.Test).value / "idml",
    (sbt.Test / test) := ((sbt.Test / test) dependsOn idmlTest).value,
    idmlTest          := {
      implicit val cs = cats.effect.IO.contextShift(global)
      val runner      = Blocker[IO].map { b => new Runner(true, None, false, b) }
      val folder      = idmlTestDirectory.value.toPath
      println(s"Executing IDML tests in $folder")
      val tests       = Files
        .walk(folder)
        .iterator()
        .asScala
        .filter(_.getParent == folder)
        .filter(_.toString.endsWith(".json"))
        .toList
      val run         = runner.use { r =>
        for {
          results <- tests.traverse(r.runTest(false)(cs))
          results2 = results.flatten
          _       <- r.report(results2)
          combined = results2.combineAll
        } yield {
          TestState.toExitCode(combined) match {
            case ExitCode.Error       =>
              throw new TestRunFailed(s"Test run failed, aggregate state is ${combined}")
            case ExitCode.Success | _ => ()
          }
        }
      }
      run.unsafeRunSync()
    }
  )

}
