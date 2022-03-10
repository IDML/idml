name := "idmldoc"

libraryDependencies ++= Seq(
  "org.tpolecat"  %% "atto-core"   % "0.9.3",
  "com.lihaoyi"   %% "fastparse"   % "2.3.2",
  "co.fs2"        %% "fs2-io"      % "2.5.9",
  "org.typelevel" %% "cats-effect" % "2.5.0",
  "com.monovore"  %% "decline"     % "1.4.0"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.8" % Test
)

mainClass in Compile := Some("io.idml.doc.Main")
