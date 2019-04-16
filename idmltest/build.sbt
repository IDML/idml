name := "idml-test"

libraryDependencies ++= Seq(
  "co.fs2"        %% "fs2-io"        % "1.0.4",
  "org.typelevel" %% "cats-effect"   % "1.2.0",
  "com.monovore"  %% "decline"       % "0.5.0",
  "io.circe"      %% "circe-generic" % "0.11.1",
  "io.circe"      %% "circe-parser"  % "0.11.1",
  "io.circe"      %% "circe-literal"  % "0.11.1" % Test,
  "io.circe"      %% "circe-yaml"    % "0.9.0",
  "com.lihaoyi"   %% "fansi"         % "0.2.5",
  "org.gnieh"     %% "diffson-circe" % "3.1.1",
  "org.tpolecat"  %% "atto-core"   % "0.6.4",
  "com.googlecode.java-diff-utils" % "diffutils" % "1.2.1",
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % Test
)

mainClass in Compile := Some("io.idml.test.Main")
