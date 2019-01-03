import scala.util.Properties
import sbtassembly.AssemblyPlugin.defaultShellScript

name := "idml-parent"

version in Global := "1.1." + Properties.envOrElse("GO_PIPELINE_COUNTER", "0")

organization in Global := "io.idml"

scalaVersion in Global := "2.12.4"

isSnapshot in Global := true

lazy val lang = project

lazy val datanodes = project

lazy val core = project.dependsOn(datanodes).dependsOn(lang).enablePlugins(BuildInfoPlugin).settings(
  buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
  buildInfoPackage := "io.idml",
 buildInfoOptions += BuildInfoOption.BuildTime
)

lazy val geo = project.dependsOn(core)

lazy val jsoup = project.dependsOn(core)

lazy val utils = project.dependsOn(core).dependsOn(geo).dependsOn(jsoup)

lazy val repl = project.dependsOn(core).dependsOn(geo).dependsOn(jsoup).dependsOn(hashing)

lazy val tool = project.dependsOn(core).dependsOn(geo).dependsOn(jsoup).dependsOn(utils).dependsOn(repl).dependsOn(idmld).dependsOn(hashing).settings(
  assemblyOption in assembly := (assemblyOption in assembly).value.copy(prependShellScript = Some(defaultShellScript))
)

lazy val geodb = project.dependsOn(core).dependsOn(geo)

lazy val idmld = project.dependsOn(core).dependsOn(geo).dependsOn(hashing).dependsOn(jsoup).dependsOn(utils)

lazy val hashing = project.dependsOn(core)
