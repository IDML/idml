package io.idml.test

import java.nio.file.Files

import sbt._
import sbt.Keys._
import cats.effect._
import cats._
import cats.implicits._
import scala.collection.JavaConverters._

object IdmlTestPlugin extends AutoPlugin {
  object autoImport {
    val idmlTest: TaskKey[Unit]             = taskKey[Unit]("run IDML tests")
    val idmlTestDirectory: SettingKey[File] = settingKey[File]("folder to run IDML tests in")
  }
  import autoImport._

  override val trigger = allRequirements

  class TestRunFailed(s: String) extends Exception {
    // This intentionally has no stack trace
    override def fillInStackTrace(): Throwable = this
    override def getMessage: String            = s
    override def getLocalizedMessage: String   = s
  }

  override val projectSettings = Seq(
    idmlTestDirectory := (sourceDirectory in sbt.Test).value / "idml",
    (sbt.Test / test) := ((sbt.Test / test) dependsOn idmlTest).value,
    idmlTest := {
      val runner = new Runner(true, None)
      val folder = idmlTestDirectory.value.toPath
      println(s"Executing IDML tests in $folder")
      val tests = Files.walk(folder).iterator().asScala.filter(_.getParent == folder).filter(_.toString.endsWith(".json")).toList
      val run = for {
        results  <- tests.traverse(runner.runTest(false))
        results2 = results.flatten
        _        <- runner.report(results2)
      } yield {
        TestState.toExitCode(results2.combineAll) match {
          case ExitCode.Error   => throw new TestRunFailed(s"Test run failed, aggregate state is ${results2.combineAll}")
          case ExitCode.Success => ()
        }
      }
      run.unsafeRunSync()
    }
  )

}
