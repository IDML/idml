name := "idml-geo"

libraryDependencies ++= Seq(
  "org.xerial"        % "sqlite-jdbc"    % "3.21.0.1",
  "org.tpolecat"      %% "doobie-core"   % "0.7.0-M3",
  "org.tpolecat"      %% "doobie-hikari" % "0.7.0-M3",
  "net.iakovlev"      % "timeshape"      % "2018d.6",
  "org.scalatest"     %% "scalatest"     % "3.0.4" % Test,
  "com.storm-enroute" %% "scalameter"    % "0.8.2" % Test
)
