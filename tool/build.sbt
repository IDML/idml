name := "idml-tool"

libraryDependencies += "com.monovore" %% "decline" % "0.5.0"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.1"

mainClass in Compile := Some("io.idml.tool.IdmlTool")
