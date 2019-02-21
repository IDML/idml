name := "idml-geo"

libraryDependencies ++= Seq(
  "org.xerial"        % "sqlite-jdbc"    % "3.21.0.1",
  "org.tpolecat"      %% "doobie-core"   % "0.6.0-M2",
  "org.tpolecat"      %% "doobie-hikari" % "0.6.0-M2",
  "org.scalatest"     %% "scalatest"     % "3.0.4" % Test,
  "com.storm-enroute" %% "scalameter"    % "0.8.2" % Test
)
