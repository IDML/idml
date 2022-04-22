name := "idml-jsoup"

libraryDependencies ++= Seq(
  "org.jsoup"          % "jsoup"      % "1.8.1",
  "org.scalatest"     %% "scalatest"  % "3.2.8" % Test,
  "com.storm-enroute" %% "scalameter" % "0.19"  % Test
)
