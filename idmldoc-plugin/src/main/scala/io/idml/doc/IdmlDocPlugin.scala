package io.idml.doc

import sbt._
import microsites._
import sbt.Keys._
import com.typesafe.sbt.site.SitePlugin.autoImport.makeSite
import cats.effect.{IO, Blocker, ContextShift}

import scala.concurrent.ExecutionContext

object IdmlDocSbt extends AutoPlugin with MicrositeKeys {

  override def requires = MicrositesPlugin

  lazy val Idml               = (config("Idml") extend Compile).hide
  val makeIdml: TaskKey[Unit] = taskKey[Unit]("Sequential tasks to compile idml and move the result")
  val idml: TaskKey[Unit]     = taskKey[Unit]("Compile the IDML")

  val idmlTargetDirectory: SettingKey[File] = settingKey[File]("folder to put IDML docs in once they're built")
  val idmlSourceDirectory: SettingKey[File] = settingKey[File]("folder to read IDML docs from")

  override val projectSettings = inConfig(Idml)(Defaults.configSettings) ++ Seq(
    idmlSourceDirectory := (Compile / sourceDirectory).value / "tut",
    idmlTargetDirectory := resourceManaged.value / "main" / "jekyll",
    idml := {
      implicit val cs: ContextShift[IO] = cats.effect.IO.contextShift(ExecutionContext.global)
      Blocker[IO].use { blocker =>
        Main.processFiles[IO](idmlSourceDirectory.value.toPath, idmlTargetDirectory.value.toPath, blocker)
      }.unsafeRunSync()
    },
    makeIdml := {
      Def.sequential(microsite, idml, makeSite)
    }.value,
  )

}
