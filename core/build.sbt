name := "idml-core"

libraryDependencies ++= Seq(
  "org.slf4j"          % "slf4j-api"   % "1.7.26",
  "org.tpolecat"      %% "atto-core"   % "0.9.4",
  "org.scalatest"     %% "scalatest"   % "3.2.8"   % Test,
  "org.mockito"        % "mockito-core"  % "4.11.0"   % Test,
  "org.scalatestplus" %% "mockito-4-11" % "3.2.18.0" % Test
)
