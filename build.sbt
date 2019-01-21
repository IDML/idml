enablePlugins(GitVersioning)

import scala.util.Properties
import sbtassembly.AssemblyPlugin.defaultShellScript

name := "idml-parent"

organization := "io.idml"

scalaVersion := "2.12.4"

publishTo := sonatypePublishTo.value

publishMavenStyle := true

isSnapshot := false

useGpg := true

licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))

import xerial.sbt.Sonatype._

lazy val commonSettings = Seq(
  organization := "io.idml",
  isSnapshot := false,
  publishTo := sonatypePublishTo.value,
  licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
  sonatypeProjectHosting := Some(GitHubHosting("idml", "idml", "opensource@meltwater.com")),
  developers := List(Developer(id="andimiller", name="Andi Miller", email="andi@andimiller.net", url=url("http://andimiller.net"))),
)

lazy val lang = project.settings(commonSettings)

lazy val datanodes = project.settings(commonSettings)

lazy val core = project
  .dependsOn(datanodes)
  .dependsOn(lang)
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "io.idml",
    buildInfoOptions += BuildInfoOption.BuildTime
  )

//lazy val geo = project.dependsOn(core)

lazy val jsoup = project.dependsOn(core).settings(commonSettings)

lazy val hashing = project.dependsOn(core).settings(commonSettings)

lazy val utils = project.dependsOn(core).dependsOn(jsoup).settings(commonSettings)

lazy val repl = project.dependsOn(core).dependsOn(jsoup).dependsOn(hashing).settings(commonSettings)

lazy val idmld = project.dependsOn(core).dependsOn(hashing).dependsOn(jsoup).dependsOn(utils).settings(commonSettings)

lazy val tool = project
  .dependsOn(core)
  .dependsOn(jsoup)
  .dependsOn(utils)
  .dependsOn(repl)
  .dependsOn(idmld)
  .dependsOn(hashing)
  .settings(commonSettings)
  .settings(
    assembly/assemblyOption := (assembly/assemblyOption).value.copy(prependShellScript = Some(defaultShellScript)),
    assembly/assemblyMergeStrategy := {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case PathList("buildinfo/BuildInfo$.class") => MergeStrategy.first
      case _ => MergeStrategy.first
    }
  )

//lazy val geodb = project.dependsOn(core).dependsOn(geo)
