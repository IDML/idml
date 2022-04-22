enablePlugins(GitVersioning)
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

import scala.util.Properties
import sbtassembly.AssemblyPlugin.defaultShellScript

name := "idml-parent"

organization := "io.idml"

publishTo := sonatypePublishTo.value

publishMavenStyle := true

isSnapshot := false

useGpg := true

publishArtifact := false

licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))

import xerial.sbt.Sonatype._

lazy val scala212               = "2.12.13"
lazy val scala213               = "2.13.5"
lazy val supportedScalaVersions = List(scala212, scala213)

lazy val commonSettings = Seq(
  organization              := "io.idml",
  crossScalaVersions        := supportedScalaVersions,
  isSnapshot                := false,
  publishArtifact           := true,
  publishTo                 := sonatypePublishTo.value,
  licenses                  := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
  sonatypeProjectHosting    := Some(GitHubHosting("idml", "idml", "opensource@meltwater.com")),
  developers                := List(
    Developer(
      id = "andimiller",
      name = "Andi Miller",
      email = "andi@andimiller.net",
      url = url("http://andimiller.net")),
    Developer(
      id = "teamrobin",
      name = "Team Robin",
      email = "teamrobin@meltwater.com",
      url = url("https://meltwater.com"))
  ),
  scalacOptions += "-target:jvm-1.8",
  Docker / version          := version.value,
  Docker / dockerUsername   := Some("idml"),
  addCompilerPlugin("org.typelevel" % "kind-projector" % "0.11.3" cross CrossVersion.full),
  scalacOptions ++= {
    import Ordering.Implicits._
    if (VersionNumber(scalaVersion.value).numbers >= Seq(2L, 13L)) {
      List("-Ymacro-annotations")
    } else {
      List("-Ypartial-unification")
    }
  },
  libraryDependencies ++= {
    import Ordering.Implicits._
    if (VersionNumber(scalaVersion.value).numbers >= Seq(2L, 13L)) {
      List.empty
    } else {
      List(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full))
    }
  },
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  git.gitTagToVersionNumber := { tag: String =>
    if (tag matches "[0-9].*") Some(tag) else None
  }
)

lazy val lang = project
  .settings(commonSettings)
  .settings(
    Antlr4 / antlr4Version     := "4.8-1",
    Antlr4 / antlr4PackageName := Some("io.idml.lang"),
    Antlr4 / antlr4GenVisitor  := true
  )

lazy val datanodes = project.settings(commonSettings)

lazy val jackson: Project =
  project.dependsOn(core % "compile->compile;test->test").settings(commonSettings)

lazy val circe = project
  .dependsOn(datanodes)
  .dependsOn(core % "compile->compile;test->test")
  .settings(commonSettings)

lazy val core: Project = project
  .dependsOn(datanodes)
  .dependsOn(lang)
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings)
  .settings(
    buildInfoKeys    := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "io.idml",
    buildInfoOptions += BuildInfoOption.BuildTime
  )

lazy val test = project
  .dependsOn(core)
  .dependsOn(jackson % "compile->test")
  .dependsOn(circe % "test->compile")
  .settings(commonSettings)
  .settings(
    publishArtifact := false
  )

lazy val geo = project
  .dependsOn(core)
  .settings(commonSettings)
  .dependsOn(jackson % "test->test")
  .dependsOn(test % "test->test")
  .settings(
    Test / fork    := true,
    Test / envVars := Map(
      "IDML_GEO_DB_DRIVER"       -> "org.sqlite.JDBC",
      "IDML_GEO_CITY_JDBC_URL"   -> "jdbc:sqlite::resource:cities.test.db",
      "IDML_GEO_ADMIN1_JDBC_URL" -> "jdbc:sqlite::resource:admin1.test.db"
    )
  )

lazy val jsoup = project.dependsOn(core).dependsOn(test % "test->test").settings(commonSettings)

lazy val hashing = project.dependsOn(core).settings(commonSettings)

lazy val utils = project
  .dependsOn(core)
  .dependsOn(jsoup)
  .dependsOn(jackson % "test->test")
  .settings(commonSettings)

lazy val repl = project
  .dependsOn(core)
  .dependsOn(jsoup)
  .dependsOn(hashing)
  .dependsOn(circe)
  .dependsOn(utils)
  .settings(commonSettings)

lazy val idmld =
  project
    .dependsOn(core)
    .dependsOn(hashing)
    .dependsOn(jsoup)
    .dependsOn(utils)
    .dependsOn(circe)
    .dependsOn(geo)
    .settings(commonSettings)

lazy val idmldoc =
  project.dependsOn(core).dependsOn(utils).dependsOn(circe).settings(commonSettings)

lazy val `idmldoc-plugin` = project
  .dependsOn(idmldoc)
  .settings(commonSettings)
  .settings(
    crossScalaVersions := List(scala212)
  )

lazy val idmltest =
  project.dependsOn(core).dependsOn(utils).dependsOn(circe).settings(commonSettings)

lazy val `idmltest-plugin` = project
  .dependsOn(idmltest)
  .dependsOn(hashing)
  .dependsOn(geo)
  .dependsOn(jsoup)
  .dependsOn(jackson)
  .settings(commonSettings)
  .settings(
    crossScalaVersions := List(scala212)
  )

lazy val idmltutor = project
  .dependsOn(hashing)
  .dependsOn(geo)
  .dependsOn(jsoup)
  .dependsOn(circe)
  .dependsOn(idmltest)
  .settings(commonSettings)
  .settings(
    dockerExposedPorts               := Seq(8081),
    Docker / packageName             := "idml",
    Docker / dockerUpdateLatest      := true,
    assembly / assemblyOption        := (assembly / assemblyOption).value.copy(prependShellScript =
      Some(defaultShellScript)),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "MANIFEST.MF")                                    => MergeStrategy.discard
      case PathList("buildinfo/BuildInfo$.class")                                 => MergeStrategy.first
      case PathList("META-INF", "services", "io.idml.functions.FunctionResolver") =>
        MergeStrategy.concat
      case _                                                                      => MergeStrategy.first
    }
  )

lazy val tool = project
  .dependsOn(core)
  .dependsOn(jsoup)
  .dependsOn(utils)
  .dependsOn(repl)
  .dependsOn(idmld)
  .dependsOn(idmltest)
  .dependsOn(hashing)
  .dependsOn(idmltutor)
  .dependsOn(geo)
  .enablePlugins(DockerPlugin, JavaAppPackaging)
  .settings(commonSettings)
  .settings(
    dockerExposedPorts               := Seq(8081),
    Docker / packageName             := "idml",
    Docker / dockerUpdateLatest      := true,
    assembly / assemblyOption        := (assembly / assemblyOption).value.copy(prependShellScript =
      Some(defaultShellScript)),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "MANIFEST.MF")                                    => MergeStrategy.discard
      case PathList("buildinfo/BuildInfo$.class")                                 => MergeStrategy.first
      case PathList("META-INF", "services", "io.idml.functions.FunctionResolver") =>
        MergeStrategy.concat
      case _                                                                      => MergeStrategy.first
    }
  )

//lazy val geodb = project.dependsOn(core).dependsOn(geo)

//lazy val docs = project
//  .settings(commonSettings)
