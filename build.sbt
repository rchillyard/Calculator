name := "PlaySampleProject"

version := "0.1"

scalaVersion := "2.11.7"

lazy val root = (project in file(".")).enablePlugins(PlayScala)



val akkaGroup = "com.typesafe.akka"
val akkaVersion = "2.3.12"
val scalaTestVersion = "2.2.4"

libraryDependencies ++= Seq(
  akkaGroup %% "akka-actor" % akkaVersion,
  akkaGroup %% "akka-testkit" % akkaVersion % Test,
  akkaGroup %% "akka-slf4j" % akkaVersion,
  "com.typesafe" % "config" % "1.3.0",
  jdbc,
  cache,
  ws,
  specs2 % Test,
//  "default" %% "numerics" % "1.0.0-SNAPSHOT",
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test

)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator


fork in run := true