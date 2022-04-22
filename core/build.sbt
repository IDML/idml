name := "idml-core"

libraryDependencies ++= Seq(
  "org.slf4j"          % "slf4j-api"   % "1.7.26",
  "org.tpolecat"      %% "atto-core"   % "0.9.4",
  "org.scalatest"     %% "scalatest"   % "3.2.8"   % Test,
  "org.mockito"        % "mockito-all" % "1.9.5"   % Test,
  "org.scalatestplus" %% "mockito-3-4" % "3.2.8.0" % Test
)
