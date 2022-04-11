name := "idml-hashing"

libraryDependencies ++= Seq(
  "net.openhft"   % "zero-allocation-hashing" % "0.8",
  "org.lz4"       % "lz4-java"                % "1.8.0",
  "org.scalatest" %% "scalatest"              % "3.2.8" % Test
)
