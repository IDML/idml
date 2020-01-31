name := "idml-tool"

libraryDependencies += "com.monovore" %% "decline" % "0.5.0"

//libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.1"
//libraryDependencies += "org.codehaus.janino" % "janino" % "2.6.1"

libraryDependencies += "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.13.0"

mainClass in Compile := Some("IdmlTool")

enablePlugins(SbtProguard)

Proguard / proguardVersion := "6.0.3"
Proguard / proguardMerge := true
Proguard / proguardMergeStrategies += ProguardMerge.discard("META-INF/.*".r)
Proguard / proguardMergeStrategies += ProguardMerge.discard("LICENSE".r)
Proguard / proguardMergeStrategies += ProguardMerge.discard("rootdoc.txt".r)
Proguard / proguardMergeStrategies += ProguardMerge.first("buildinfo/BuildInfo$.class")
Proguard / proguardOptions ++= Seq(
  "-dontobfuscate",
  "-dontoptimize",
  "-dontnote",
  "-dontwarn",
  "-ignorewarnings",
  "-keep class scala.Symbol {*;}"
)

Proguard / proguardInputs  := (dependencyClasspath in Compile).value.files
Proguard / proguardMergedInputs ++= ProguardOptions.noFilter((packageBin in Compile).value)
Proguard / proguard / javaOptions := Seq("-Xmx2g")
Proguard / proguardOutputs := Seq(new File(target.value, "tool-proguard.jar"))


proguardOptions in Proguard += ProguardOptions.keepMain("IdmlTool")
