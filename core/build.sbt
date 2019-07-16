name := "idml-core"

libraryDependencies ++= Seq(
  "commons-io"        % "commons-io"  % "2.6",
  "org.slf4j" % "slf4j-api" % "1.7.26",
  "org.tpolecat" %% "atto-core" % "0.6.4",
  "org.scalatest"     %% "scalatest"  % "3.0.4" % Test,
  "org.mockito"   % "mockito-all" % "1.9.5" % Test,
)
