name := "idmldoc"

libraryDependencies ++= Seq(
  "org.tpolecat"  %% "atto-core"   % "0.6.4",
  "com.lihaoyi"   %% "fastparse"   % "2.1.0",
  "co.fs2" %% "fs2-io" % "1.0.0-M1",
  "org.typelevel" %% "cats-effect" % "1.0.0-RC2",
  "com.monovore" %% "decline" % "0.5.0"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % Test
)

mainClass in Compile := Some("io.idml.doc.Main")
