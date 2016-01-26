name := """tyrion"""

routesGenerator := InjectedRoutesGenerator
herokuAppName in Compile := "byzance3"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc4",
  "com.microsoft.azure" % "adal4j" % "1.1.2",
  "com.microsoft.azure" % "azure-core" % "0.9.1",
  "com.microsoft.azure" % "azure-storage" % "4.0.0",
  "com.microsoft.azure" % "azure-svc-mgmt" % "0.9.1",
  "io.swagger" %% "swagger-play2" % "1.5.1" exclude("org.reflections", "reflections"),
  "io.swagger" % "swagger-core" % "1.5.6",
  "be.objectify"  %% "deadbolt-java"     % "2.4.3",
  "com.github.scribejava" % "scribejava-apis" % "2.1.0",
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


// add resolver for deadbolt and easymail snapshots
resolvers += Resolver.sonatypeRepo("snapshots")

// display deprecated or poorly formed Java
javacOptions ++= Seq("-Xlint:unchecked")
javacOptions ++= Seq("-Xlint:deprecation")
javacOptions ++= Seq("-Xdiags:verbose")

//  Uncomment the next line for local development of the Play Authenticate core:
//lazy val playAuthenticate = project.in(file("modules/play-authenticate")).enablePlugins(PlayJava)

