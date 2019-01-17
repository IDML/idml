enablePlugins(GitVersioning)

import scala.util.Properties
import sbtassembly.AssemblyPlugin.defaultShellScript

name := "idml-parent"

organization in Global := "io.idml"

scalaVersion in Global := "2.12.4"

isSnapshot in Global := true

lazy val lang = project

lazy val datanodes = project

lazy val core = project
  .dependsOn(datanodes)
  .dependsOn(lang)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "io.idml",
    buildInfoOptions += BuildInfoOption.BuildTime
  )

//lazy val geo = project.dependsOn(core)

lazy val jsoup = project.dependsOn(core)

lazy val hashing = project.dependsOn(core)

lazy val utils = project.dependsOn(core).dependsOn(jsoup)

lazy val repl = project.dependsOn(core).dependsOn(jsoup).dependsOn(hashing)

lazy val idmld = project.dependsOn(core).dependsOn(hashing).dependsOn(jsoup).dependsOn(utils)

lazy val idmldoc = project.dependsOn(core).dependsOn(utils)

lazy val tool = project
  .dependsOn(core)
  .dependsOn(jsoup)
  .dependsOn(utils)
  .dependsOn(repl)
  .dependsOn(idmld)
  .dependsOn(hashing)
  .settings(
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(prependShellScript = Some(defaultShellScript)),
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case PathList("buildinfo/BuildInfo$.class") => MergeStrategy.first
      case _ => MergeStrategy.first
    }
  )

//lazy val geodb = project.dependsOn(core).dependsOn(geo)
