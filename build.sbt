name := "kalah"

version := "1.0"

lazy val `kalah` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

val akkaVersion = "2.4.11"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  javaWs,
  specs2 % Test,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
  "dom4j" % "dom4j" % "1.6.1",
  "de.grundid.opendatalab" % "geojson-jackson" % "1.1",
  "org.hamcrest" % "hamcrest-all" % "1.3" % "test",
  "org.mockito" %  "mockito-all" % "1.9.5" % "test"
)

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"


