name := "idml-jsoup"

libraryDependencies ++= Seq(
  "org.jsoup"         % "jsoup"       % "1.8.1",
  "org.scalatest"     %% "scalatest"  % "3.0.2" % Test,
  "com.storm-enroute" %% "scalameter" % "0.8.2" % Test
)
