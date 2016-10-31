name := """Tyrion"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)



scalaVersion := "2.11.6"
crossScalaVersions := Seq("2.11.6", "2.11.7")

libraryDependencies ++= Seq(

  "org.postgresql" % "postgresql" % "9.4-1201-jdbc4",
  "com.microsoft.azure" % "adal4j" % "1.1.2",
  "com.microsoft.azure" % "azure-core" % "0.9.1",
  "com.microsoft.azure" % "azure-storage" % "4.0.0",
  "com.microsoft.azure" % "azure-svc-mgmt" % "0.9.1",

  "io.swagger" %% "swagger-play2" % "1.5.2",

  "com.typesafe.play" %% "routes-compiler"            % "2.4.6",
  "io.swagger"         % "swagger-core"               % "1.5.8",
  "io.swagger"        %% "swagger-scala-module"       % "1.0.2",

  "com.github.scribejava" % "scribejava-apis" % "2.1.0",
  "com.typesafe.play" %% "play-mailer" % "4.0.0-M1",

  "mysql" % "mysql-connector-java" % "5.1.18",
  "net.sourceforge.jtds" % "jtds" % "1.3.0",
  "commons-codec" % "commons-codec" % "1.9",
  "com.cedarsoftware" % "json-io" % "4.4.0",
  "org.pegdown" % "pegdown" % "1.6.0",
  "net.sf.ehcache" % "ehcache" % "2.10.2",
  "com.microsoft.azure" % "adal4j" % "1.1.3",
  "com.google.guava" % "guava" % "19.0",
  "commons-collections" % "commons-collections" % "3.2.1",
  "mysql" % "mysql-connector-java" % "5.1.34",
  "org.quartz-scheduler" % "quartz" % "2.2.3",

  "com.github.nkzawa" % "socket.io-client" % "0.1.2",

  "com.novocode" % "junit-interface" % "0.11",

  "junit" % "junit" % "4.12" ,
  "org.mockito" % "mockito-core" % "1.10.19" % "test",

  javaJdbc,
  filters,
  cache,
  javaWs
)

TwirlKeys.templateImports += "utilities.loggy._"


// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
