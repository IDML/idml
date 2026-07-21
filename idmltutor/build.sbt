name := "idmltutor"

libraryDependencies ++= List(
  "org.jline"     % "jline"         % "3.13.3",
  "org.tpolecat" %% "atto-core"     % "0.9.4",
  "com.lihaoyi"  %% "fansi"         % "0.2.13",
  "io.circe"     %% "circe-literal" % "0.14.1"
)

libraryDependencies ++= Seq(
  "org.mockito"    % "mockito-core" % "4.11.0" % Test,
  "org.scalatest" %% "scalatest"   % "3.2.8" % Test
)

mainClass in Compile := Some("io.idml.tutor.Main")
