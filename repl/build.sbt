name := "idml-repl"

// taken from https://github.com/sbt/sbt-assembly/issues/92
libraryDependencies +=
  ("org.scala-lang.modules" % "scala-jline" % "2.12.1") //scalaVersion.value)

libraryDependencies ++= Seq(
  "org.mockito"   % "mockito-all" % "1.9.5" % Test,
  "org.scalatest" %% "scalatest"  % "3.0.4" % Test
)

mainClass in Compile := Some("io.idml.repl.Main")
