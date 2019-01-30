package io.idml.doc

import sbt._
import microsites._
import sbt.Keys._
import com.typesafe.sbt.site.SitePlugin.autoImport.{makeSite, siteDirectory}

object IdmlDocSbt extends MicrositeKeys {

  val makeIdml: TaskKey[Unit] = taskKey[Unit]("Sequential tasks to compile idml and move the result")
  val idml: TaskKey[Unit]     = taskKey[Unit]("Compile the IDML")

  idml := {}

  makeIdml := {
    Def.sequential(microsite, idml, micrositeTutExtraMdFiles, makeSite, micrositeConfig)
  }.value
}
