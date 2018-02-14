name := """tyrion"""

version := "2.0.0"

packageName in Universal := "v" + version.value

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc4",
  "com.microsoft.azure" % "azure-storage" % "6.1.0",
  "com.microsoft.azure" % "azure-documentdb" % "1.15.1",
  "com.mandrillapp.wrapper.lutung" % "lutung" % "0.0.8",
  "com.itextpdf" % "itextpdf" % "5.5.12",
  "org.ehcache" % "ehcache" % "3.4.0",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.reflections" % "reflections" % "0.9.11",
  "io.swagger" %% "swagger-play2" % "1.6.0",
  "org.quartz-scheduler" % "quartz" % "2.3.0",
  "com.github.scribejava" % "scribejava-apis" % "2.1.0",
  "org.assertj" % "assertj-core" % "3.6.2" % Test,
  "org.awaitility" % "awaitility" % "2.0.0" % Test,
  guice,
  ws
)

// Make verbose tests
testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))