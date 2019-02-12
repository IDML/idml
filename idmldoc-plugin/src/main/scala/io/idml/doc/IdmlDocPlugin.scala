package io.idml.doc

import sbt._
import microsites._
import sbt.Keys._
import com.typesafe.sbt.site.SitePlugin.autoImport.{makeSite, siteDirectory}
import cats.effect.IO

object IdmlDocSbt extends AutoPlugin with MicrositeKeys {

  override def requires = MicrositesPlugin

  lazy val Idml               = (config("Idml") extend Compile).hide
  val makeIdml: TaskKey[Unit] = taskKey[Unit]("Sequential tasks to compile idml and move the result")
  val idml: TaskKey[Unit]     = taskKey[Unit]("Compile the IDML")

  val idmlTargetDirectory: SettingKey[File] = settingKey[File]("folder to put IDML docs in once they're built")
  val idmlSourceDirectory: SettingKey[File] = settingKey[File]("folder to read IDML docs from")

  override val projectSettings = inConfig(Idml)(Defaults.configSettings) ++ Seq(
    idmlSourceDirectory := (sourceDirectory in Compile).value / "tut",
    idmlTargetDirectory := resourceManaged.value / "main" / "jekyll",
    idml := {
      Main.processFiles[IO](idmlSourceDirectory.value.toPath, idmlTargetDirectory.value.toPath).unsafeRunSync()
    },
    makeIdml := {
      Def.sequential(microsite, idml, makeSite, micrositeConfig)
    }.value,
  )

}
