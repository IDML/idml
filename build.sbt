enablePlugins(GitVersioning)
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

import scala.util.Properties
import sbtassembly.AssemblyPlugin.defaultShellScript

name := "idml-parent"

organization := "io.idml"

scalaVersion := "2.12.4"

publishTo := sonatypePublishTo.value

publishMavenStyle := true

isSnapshot := false

useGpg := true

publishArtifact := false

licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))

import xerial.sbt.Sonatype._

lazy val commonSettings = Seq(
  organization := "io.idml",
  isSnapshot := false,
  publishArtifact := true,
  publishTo := sonatypePublishTo.value,
  licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
  sonatypeProjectHosting := Some(GitHubHosting("idml", "idml", "opensource@meltwater.com")),
  developers := List(Developer(id="andimiller", name="Andi Miller", email="andi@andimiller.net", url=url("http://andimiller.net"))),
  version in Docker := version.value,
  dockerUsername in Docker := Some("idml"),
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

lazy val geo = project.dependsOn(core).settings(
  fork in Test := true,
  envVars in Test := Map(
    "IDML_GEO_DB_DRIVER" -> "org.sqlite.JDBC",
    "IDML_GEO_CITY_JDBC_URL" -> "jdbc:sqlite::resource:cities.test.db",
    "IDML_GEO_ADMIN1_JDBC_URL" -> "jdbc:sqlite::resource:admin1.test.db"
  )
)

lazy val jsoup = project.dependsOn(core).settings(commonSettings)

lazy val hashing = project.dependsOn(core).settings(commonSettings)

lazy val utils = project.dependsOn(core).dependsOn(jsoup).settings(commonSettings)

lazy val repl = project.dependsOn(core).dependsOn(jsoup).dependsOn(hashing).settings(commonSettings)

lazy val idmld = project.dependsOn(core).dependsOn(hashing).dependsOn(jsoup).dependsOn(utils).settings(commonSettings)

lazy val idmldoc = project.dependsOn(core).dependsOn(utils).settings(commonSettings)

lazy val `idmldoc-plugin` = project.dependsOn(idmldoc).settings(commonSettings)

lazy val tool = project
  .dependsOn(core)
  .dependsOn(jsoup)
  .dependsOn(utils)
  .dependsOn(repl)
  .dependsOn(idmld)
  .dependsOn(hashing)
  .dependsOn(geo)
  .enablePlugins(DockerPlugin, JavaAppPackaging)
  .settings(commonSettings)
  .settings(
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(prependShellScript = Some(defaultShellScript)),
    dockerExposedPorts := Seq(8081),
    packageName in Docker := "idml",
    dockerUpdateLatest in Docker := true,
    assembly/assemblyOption := (assembly/assemblyOption).value.copy(prependShellScript = Some(defaultShellScript)),
    assembly/assemblyMergeStrategy := {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case PathList("buildinfo/BuildInfo$.class") => MergeStrategy.first
      case PathList("META-INF", "services", "io.idml.functions.FunctionResolver") => MergeStrategy.concat
      case _ => MergeStrategy.first
    }
  )

//lazy val geodb = project.dependsOn(core).dependsOn(geo)
