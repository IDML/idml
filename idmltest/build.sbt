name := "idml-test"

libraryDependencies ++= Seq(
  "co.fs2"        %% "fs2-io"        % "1.0.0-M1",
  "org.typelevel" %% "cats-effect"   % "1.0.0-RC2",
  "com.monovore"  %% "decline"       % "0.5.0",
  "io.circe"      %% "circe-generic" % "0.11.1",
  "io.circe"      %% "circe-parser"  % "0.11.1",
  "io.circe"      %% "circe-yaml"    % "0.9.0",
  "com.lihaoyi"   %% "fansi"         % "0.2.5",
  "org.gnieh"     %% "diffson-circe" % "3.1.1"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % Test
)

mainClass in Compile := Some("io.idml.test.Main")
