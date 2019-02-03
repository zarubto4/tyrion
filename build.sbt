name := """tyrion"""

version := "2.3.7"

packageName in Universal := "v" + version.value

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.12.7"

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "42.2.5",
  "com.mandrillapp.wrapper.lutung" % "lutung" % "0.0.8",
  "com.itextpdf" % "itextpdf" % "5.5.12",
  "org.ehcache" % "ehcache" % "3.4.0",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.reflections" % "reflections" % "0.9.11",
  "io.swagger" % "swagger-play2" % "1.6.0",
  "io.swagger" % "swagger-scala-module" % "1.0.4",
  "org.quartz-scheduler" % "quartz" % "2.3.0",
  "com.github.scribejava" % "scribejava-apis" % "5.0.0",    // Social Network
  "org.assertj" % "assertj-core" % "3.6.2" % Test,
  "org.awaitility" % "awaitility" % "2.0.0" % Test,
  "com.myjeeva.digitalocean" % "digitalocean-api-client" % "2.13", // Digital Ocean - Creating Automaticaly Homer Servers
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-xml" % "2.8.11", // XML to JSON support
  "org.mockito" % "mockito-core" % "2.18.3" % Test,
  "org.apache.poi" % "poi" % "3.17", // Excel support for report P&G
  "org.apache.poi" % "poi-ooxml" % "3.17",
  "org.mongodb" % "mongodb-driver-reactivestreams" % "1.10.0",
  "org.mongodb" % "mongodb-driver" % "3.9.1",
  "org.mongodb" % "mongodb-driver-async" % "3.9.1",
  "xyz.morphia.morphia" % "core" % "1.4.0",
  "com.facebook.business.sdk" % "facebook-java-business-sdk" % "3.0.0",
  "com.sendgrid" % "sendgrid-java" % "4.0.1",
  "commons-validator" % "commons-validator" % "1.6", // Kontrola Emailů, URL adres etc
  "com.facebook.business.sdk" % "facebook-java-business-sdk" % "3.0.0",
  "com.restfb" % "restfb" % "2.9.0", // New Login
  "com.amazonaws" % "aws-java-sdk" % "1.11.479", // Amazon == DigitalOcean!
  evolutions,
  guice,
  ws
)

// Make verbose tests
testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))

packageName in Universal := "dist"
