name := "test"

publishArtifact := false

libraryDependencies ++= List(
  "org.scalatest"     %% "scalatest"     % "3.2.8"   % Test,
  "org.mockito"        % "mockito-all"   % "1.9.0"   % Test,
  "org.scalatestplus" %% "mockito-3-4"   % "3.2.8.0" % Test,
  "com.storm-enroute" %% "scalameter"    % "0.19"    % Test,
  "io.circe"          %% "circe-testing" % "0.13.0"  % Test,
  "org.scalatest"     %% "scalatest"     % "3.2.8"   % Test
)
