name := "idmldoc-plugin"

sbtPlugin := true

libraryDependencies += "com.lihaoyi"   %% "fastparse"   % "1.0.0"

addSbtPlugin("com.47deg" % "sbt-microsites" % "0.8.0" % Provided)
