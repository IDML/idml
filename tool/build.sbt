name := "idml-tool"

enablePlugins(NativeImagePlugin)

libraryDependencies ++= List(
  "com.monovore" %% "decline" % "1.4.0"
)

fork                := true
Compile / mainClass := Some("io.idml.tool.IdmlTool")

nativeImageInstalled  := true
nativeImageAgentMerge := true
nativeImageOptions += "--no-fallback"
nativeImageOptions += "--static"
nativeImageOptions += s"-H:ReflectionConfigurationFiles=${target.value / "native-image-configs" / "reflect-config.json"}"
nativeImageOptions += s"-H:ConfigurationFileDirectories=${target.value / "native-image-configs"}"
nativeImageOptions += "-H:+JNI"
nativeImageOptions += "--allow-incomplete-classpath"
