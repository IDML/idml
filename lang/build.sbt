name := "idml-lang"

enablePlugins(Antlr4Plugin)

antlr4GenListener in Antlr4 := true

antlr4GenVisitor in Antlr4 := true

antlr4PackageName in Antlr4 := Some("io.idml.lang")

antlr4Version in Antlr4 := "4.10.1"

antlr4Dependency in Antlr4 :=
  "org.antlr" % "antlr4" % "4.10.1"

libraryDependencies ++= Seq(
  "com.google.guava" % "guava"     % "27.0-jre", // Apache License 2
  "org.scalatest"   %% "scalatest" % "3.2.8" % Test // Apache License 2
)
