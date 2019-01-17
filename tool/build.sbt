name := "idml-tool"

libraryDependencies += "com.monovore" %% "decline" % "0.4.2"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.1"

mainClass in Compile := Some("IdmlTool")
