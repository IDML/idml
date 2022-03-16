name := "idml-lang"

enablePlugins(Antlr4Plugin)

antlr4GenListener in Antlr4 := true

antlr4GenVisitor in Antlr4 := true

antlr4Version in Antlr4 := "4.5"

antlr4Dependency in Antlr4 :=
  "org.antlr" % "antlr4" % "4.5" exclude ("org.antlr", "ST4") exclude ("org.antlr", "antlr-runtime") // BSD license

libraryDependencies ++= Seq(
  "com.google.guava" % "guava"      % "27.0-jre", // Apache License 2
  "org.scalatest"    %% "scalatest" % "3.2.8" % Test // Apache License 2
)
