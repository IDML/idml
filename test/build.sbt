name := "test"

publishArtifact := false

libraryDependencies ++= List(
  "org.scalatest"     %% "scalatest"  % "3.0.4" % Test,
  "org.mockito"       % "mockito-all" % "1.9.0" % Test,
  "com.storm-enroute" %% "scalameter" % "0.8.2" % Test,
  "io.circe" %% "circe-testing" % "0.11.1" % Test,
  "org.scalatest" %% "scalatest" % "3.0.7" % Test
)
