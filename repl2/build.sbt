name := "idml-repl2"

libraryDependencies ++= Seq(
  "co.fs2"        %% "fs2-io"        % "1.0.4",
  "org.typelevel" %% "cats-mtl-core" % "0.5.0",
  "org.typelevel" %% "cats-tagless-macros" % "0.5",
  "org.typelevel" %% "cats-effect" % "1.2.0",
  "com.lihaoyi"  %% "fansi"         % "0.2.7",
  "org.jline" % "jline-terminal-jna" % "3.13.3",
  "org.jline" % "jline-reader" % "3.13.3",
  "org.mockito"   % "mockito-all" % "1.9.5" % Test,
  "org.scalatest" %% "scalatest"  % "3.0.4" % Test
)

 addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

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
  "-keep class scala.Symbol {*;}",
  "-keep public class org.slf4j.* {*;}",
  "-keep public class ch.qos.logback.* { *; }",
  "-keep public class org.slf4j.* { *; }",
  """-keepclassmembers class * extends java.lang.Enum {
    |    <fields>;
    |    public static **[] values();
    |    public static ** valueOf(java.lang.String);
    |}""".stripMargin
)

Proguard / proguardInputs  := (dependencyClasspath in Compile).value.files
Proguard / proguardMergedInputs ++= ProguardOptions.noFilter((packageBin in Compile).value)
Proguard / proguard / javaOptions := Seq("-Xmx2g")
Proguard / proguardOutputs := Seq(new File(target.value, "repl2-proguard.jar"))


proguardOptions in Proguard += ProguardOptions.keepMain("io.idmlrepl.Main")
