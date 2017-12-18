name := """Tyrion"""

version := "1.13.28"

packageName in Universal := "v" + version.value

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(

  "org.postgresql" % "postgresql" % "9.4-1201-jdbc4",
  "com.microsoft.azure" % "azure-core" % "0.9.1",
  "com.microsoft.azure" % "azure-storage" % "4.0.0",
  "com.microsoft.azure" % "azure-svc-mgmt" % "0.9.1",

  "io.swagger" %% "swagger-play2" % "1.5.2",

  "com.typesafe.play" %% "routes-compiler"            % "2.4.6",
  "io.swagger"         % "swagger-core"               % "1.5.12",
  "io.swagger"        %% "swagger-scala-module"       % "1.0.3",

  "com.github.scribejava" % "scribejava-apis" % "4.1.1",

  "commons-codec" % "commons-codec" % "1.9",
  "com.cedarsoftware" % "json-io" % "4.4.0",
  "org.pegdown" % "pegdown" % "1.6.0",
  "com.microsoft.azure" % "adal4j" % "1.1.3",
  "com.google.guava" % "guava" % "19.0",
  "commons-collections" % "commons-collections" % "3.2.1",

  "org.quartz-scheduler" % "quartz" % "2.2.3",
  "org.ehcache" % "ehcache-clustered" % "3.3.0",
  "com.github.nkzawa" % "socket.io-client" % "0.1.2",

  "com.novocode" % "junit-interface" % "0.11",

  "junit" % "junit" % "4.12" ,
  "org.mockito" % "mockito-core" % "1.10.19" % "test",

  "com.mandrillapp.wrapper.lutung" % "lutung" % "0.0.7",

  "com.microsoft.azure" % "azure-documentdb" % "1.10.0",

  "com.graphql-java" % "graphql-java" % "2.4.0",

  "org.mindrot" % "jbcrypt" % "0.3m",

  "com.itextpdf" % "itextpdf" % "5.5.12",
  "com.itextpdf" % "barcodes" % "7.0.4",
  "com.itextpdf.tool" % "xmlworker" % "5.5.12",

  javaJdbc,
  filters,
  javaWs,
  evolutions
)

resolvers += "JBoss" at "https://repository.jboss.org/"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator