name := "idml-test"

libraryDependencies ++= Seq(
  "co.fs2"        %% "fs2-io"              % "2.5.5",
  "org.typelevel" %% "cats-effect"   % "2.5.0",
  "com.monovore"  %% "decline"       % "1.4.0",
  "io.circe"      %% "circe-generic" % "0.14.1",
  "io.circe"      %% "circe-parser"  % "0.14.1",
  "io.circe"      %% "circe-literal"  % "0.14.1" % Test,
  "io.circe"      %% "circe-yaml"    % "0.13.1",
  "com.lihaoyi"   %% "fansi"         % "0.2.13",
  "org.gnieh"     %% "diffson-circe" % "4.1.1",
  "org.tpolecat"  %% "atto-core"   % "0.9.4",
  "com.googlecode.java-diff-utils" % "diffutils" % "1.2.1",
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.8" % Test
)

mainClass in Compile := Some("io.idml.test.Main")
