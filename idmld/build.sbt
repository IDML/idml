name := "idmld"

lazy val http4sVersion = "0.20.0-RC1"
lazy val circeVersion  = "0.11.1"

libraryDependencies ++= Seq(
  "org.http4s"     %% "http4s-core"         % http4sVersion,
  "org.http4s"     %% "http4s-dsl"          % http4sVersion,
  "org.http4s"     %% "http4s-circe"        % http4sVersion,
  "org.http4s"     %% "http4s-blaze-server" % http4sVersion,
  "io.circe"       %% "circe-generic"       % circeVersion,
  "io.circe"       %% "circe-parser"        % circeVersion,
  "ch.qos.logback" % "logback-classic"      % "1.0.1"
)
