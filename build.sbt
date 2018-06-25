name := """tyrion"""

version := "2.2.6"

packageName in Universal := "v" + version.value

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "42.2.2",
  "com.microsoft.azure" % "azure-storage" % "6.1.0",
  "com.microsoft.azure" % "azure-documentdb" % "1.15.1",
  "com.mandrillapp.wrapper.lutung" % "lutung" % "0.0.8",
  "com.itextpdf" % "itextpdf" % "5.5.12",
  "org.ehcache" % "ehcache" % "3.4.0",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.reflections" % "reflections" % "0.9.11",
  "io.swagger" %% "swagger-play2" % "1.6.0",
  "org.quartz-scheduler" % "quartz" % "2.3.0",
  "com.github.scribejava" % "scribejava-apis" % "5.0.0",    // Social Network
  "org.assertj" % "assertj-core" % "3.6.2" % Test,
  "org.awaitility" % "awaitility" % "2.0.0" % Test,
  "com.myjeeva.digitalocean" % "digitalocean-api-client" % "2.13", // Digital Ocean - Creating Automaticaly Homer Servers
  "io.minio" % "minio" % "0.2.4", // Minio BLOB storage library - azure-storag opensource alternative
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-xml" % "2.8.0", // XML to JSON support
  "org.mongodb" % "mongodb-driver" % "3.7.0-rc0",
  "org.apache.poi" % "poi" % "3.17", // Excel support for report P&G
  "org.apache.poi" % "poi-ooxml" % "3.17",
  evolutions,
  guice,
  ws
)

// Make verbose tests
testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))