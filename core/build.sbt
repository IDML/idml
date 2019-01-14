name := "idml-core"

libraryDependencies ++= Seq(
  "commons-io"        % "commons-io"  % "2.6",
  "org.scalatest"     %% "scalatest"  % "3.0.4" % Provided,
  "org.mockito"       % "mockito-all" % "1.9.0" % Test,
  "com.storm-enroute" %% "scalameter" % "0.8.2" % Provided
)
