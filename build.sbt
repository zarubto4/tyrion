name := """Tyrion"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc4",
  "com.microsoft.azure" % "adal4j" % "1.1.2",
  "com.microsoft.azure" % "azure-core" % "0.9.1",
  "com.microsoft.azure" % "azure-storage" % "4.0.0",
  "com.microsoft.azure" % "azure-svc-mgmt" % "0.9.1",

  "io.swagger" %% "swagger-play2" % "1.5.1",
  "io.swagger" % "swagger-core"  % "1.5.7",

  "com.github.scribejava" % "scribejava-apis" % "2.1.0",
  "com.typesafe.play" %% "play-mailer" % "4.0.0-M1",
  "org.glassfish.grizzly" % "grizzly-http-server" % "2.3.23",
  "javax.websocket" % "javax.websocket-api" % "1.1",
  "org.glassfish.tyrus" % "tyrus-client" % "1.12",
  "org.glassfish.tyrus" % "tyrus-container-grizzly-client" % "1.12",
  "org.glassfish.tyrus" % "tyrus-core" % "1.12",
  "mysql" % "mysql-connector-java" % "5.1.18",
  "net.sourceforge.jtds" % "jtds" % "1.3.0",
  "commons-codec" % "commons-codec" % "1.9",

  "org.pegdown" % "pegdown" % "1.6.0",


  javaJdbc,
  filters,
  cache,
  javaWs
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
