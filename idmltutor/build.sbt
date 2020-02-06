name := "idmltutor"

libraryDependencies ++= List(
  "org.jline"    % "jline"          % "3.13.3",
  "org.tpolecat" %% "atto-core"     % "0.6.5",
  "com.lihaoyi"  %% "fansi"         % "0.2.7",
  "io.circe"     %% "circe-literal" % "0.11.1"
)

libraryDependencies ++= Seq(
  "org.mockito"   % "mockito-all" % "1.9.5" % Test,
  "org.scalatest" %% "scalatest"  % "3.0.4" % Test
)

mainClass in Compile := Some("io.idml.tutor.Main")
