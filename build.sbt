lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """Calculator""",
    version := "2.8.15",
    scalaVersion := "2.13.8",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
      // https://mvnrepository.com/artifact/com.typesafe.akka/akka-testkit
      "com.typesafe.akka" %% "akka-testkit" % "2.6.19" % Test
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings",
      "-target:jvm-1.11"
    )
  )

