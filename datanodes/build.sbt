name := "idml-datanodes"

libraryDependencies ++= Seq(
  "javax.mail"       % "mail"         % "1.5.0-b01", // FIXME: Create email plugin
  "com.google.guava" % "guava"        % "27.0-jre",
  "org.typelevel"    %% "spire"       % "0.16.0",
  "org.typelevel"    %% "cats-core"   % "1.5.0",
  "com.google.re2j"  % "re2j"         % "1.2",
  "org.scalatest"    %% "scalatest"   % "3.0.4" % Test
)
