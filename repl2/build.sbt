name := "idml-repl2"

libraryDependencies ++= Seq(
  "com.lihaoyi"  %% "fansi"         % "0.2.7",
  "org.jline" % "jline-terminal-jna" % "3.13.3",
  "org.jline" % "jline-reader" % "3.13.3",
  "org.mockito"   % "mockito-all" % "1.9.5" % Test,
  "org.scalatest" %% "scalatest"  % "3.0.4" % Test
)

mainClass in Compile := Some("io.idmlrepl.Main")

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
Proguard / proguardOutputs := Seq(new File(target.value, "repl2-proguard.jar"))


proguardOptions in Proguard += ProguardOptions.keepMain("io.idmlrepl.Main")
