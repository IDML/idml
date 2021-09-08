name := "idml-tool"

enablePlugins(UniversalPlugin)


libraryDependencies ++= List(
  "com.monovore" %% "decline" % "1.4.0",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.13.0",
)

mainClass in Compile := Some("IdmlTool")

enablePlugins(SbtProguard)

Proguard / proguardVersion := "6.0.3"
Proguard / proguardMerge := true
Proguard / proguardMergeStrategies += ProguardMerge.discard("META-INF/.*".r)
Proguard / proguardMergeStrategies += ProguardMerge.discard("LICENSE".r)
Proguard / proguardMergeStrategies += ProguardMerge.discard("rootdoc.txt".r)
Proguard / proguardMergeStrategies += ProguardMerge.first("buildinfo/BuildInfo$.class")
Proguard / proguardMergeStrategies += ProguardMerge.first("org/slf4j/impl/StaticMDCBinder.class")
Proguard / proguardMergeStrategies += ProguardMerge.first("org/slf4j/impl/StaticMarkerBinder.class")
Proguard / proguardMergeStrategies += ProguardMerge.first("org/slf4j/impl/StaticLoggerBinder.class")
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
Proguard / proguardOutputs := Seq(new File(target.value, "tool-proguard.jar"))


proguardOptions in Proguard += ProguardOptions.keepMain("IdmlTool")
