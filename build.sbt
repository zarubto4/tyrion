name := """tyrion"""

routesGenerator := InjectedRoutesGenerator
herokuAppName in Compile := "byzance3"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc4",
  "com.google.code.gson" % "gson" % "2.2.4",
  "org.eclipse.persistence" % "javax.persistence" % "2.1.1",
  "javax.xml.bind" % "jaxb-api" % "2.2.12",
  "org.apache.camel" % "camel-xmljson" % "2.16.1",
  "org.json" % "json" % "20151123",
  javaJdbc,
  filters,
  cache,
  javaWs
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

scalacOptions ++= Seq(
// Show warning feature details in the console "-feature",
// Enable routes file splitting
"-language:reflectiveCalls"
)
