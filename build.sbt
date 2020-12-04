lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """Calculator""",
    version := "2.8.x",
    scalaVersion := "2.13.1",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )

//val akkaGroup = "com.typesafe.akka"
//val akkaVersion = "2.0.5"
//val scalaTestVersion = "2.2.4"
//
//libraryDependencies ++= Seq("org.specs2" %% "specs2-core" % "4.3.4" % "test")
//
//scalacOptions in Test ++= Seq("-Yrangepos")
//
//libraryDependencies += guice
//libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
//libraryDependencies += "com.h2database" % "h2" % "1.4.197"

//libraryDependencies ++= Seq(
//  akkaGroup %% "akka-actor" % akkaVersion,
//  akkaGroup %% "akka-testkit" % akkaVersion % Test,
//  akkaGroup %% "akka-slf4j" % akkaVersion,
//  "com.typesafe" % "config" % "1.3.0",
//  jdbc,
//  cache,
//  ws,
//  specs2 % Test,
////  "default" %% "numerics" % "1.0.0-SNAPSHOT",
//  "org.scalatest" %% "scalatest" % scalaTestVersion % Test
//
//)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
//routesGenerator := InjectedRoutesGenerator


//fork in run := true