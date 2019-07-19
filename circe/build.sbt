name := "idml-circe"

libraryDependencies ++= List(
  "io.circe" %% "circe-core" % "0.11.1",
  "io.circe" %% "circe-parser" % "0.11.1",
  "io.circe" %% "circe-testing" % "0.11.1" % "test",
  "org.scalatest" %% "scalatest" % "3.0.7" % "test",
)