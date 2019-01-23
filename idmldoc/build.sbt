name := "idmldoc"

libraryDependencies ++= Seq(
  "org.tpolecat"  %% "atto-core"   % "0.6.4",
  "com.lihaoyi"   %% "fastparse"   % "1.0.0",
  "org.typelevel" %% "cats-effect" % "1.0.0-RC2"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % Test
)

mainClass in Compile := Some("io.idml.doc.Main")
