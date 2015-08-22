

val commonSettings = Seq(
  organization := "your.organization",
  version := "2.3.10",
  scalaVersion := "2.11.6",

  // build info
  buildInfoPackage := "meta",
  buildInfoOptions += BuildInfoOption.ToJson,
  buildInfoKeys := Seq[BuildInfoKey](
    name, version, scalaVersion,
    "sbtNativePackager" -> "1.0.0"
  )
)

lazy val root = (project in file("."))
  .settings(
    name := """akka-play-reactive-sse"""
  )
  .aggregate(api, frontend, backend)
  
lazy val frontend = (project in file("frontend"))
    .enablePlugins(PlayScala, BuildInfoPlugin, JavaAppPackaging)
    .settings(
        name := "cluster-play-frontend",
        libraryDependencies ++= (Dependencies.frontend  ++ Seq(filters, cache)),
        pipelineStages := Seq(rjs, digest, gzip),
        javaOptions ++= Seq("-Xms128m", "-Xmx1024m"),
        fork in run := true,
        commonSettings
    ).dependsOn(api)

lazy val backend = (project in file("backend"))
    .enablePlugins(BuildInfoPlugin, JavaAppPackaging)
    .settings(
        name := "cluster-akka-backend",
        libraryDependencies ++= Dependencies.backend,
        javaOptions ++= Seq("-Xms128m", "-Xmx1024m"),
        fork in run := true,
        commonSettings
    ).dependsOn(api)
    
lazy val api = (project in file("api"))
    .enablePlugins(BuildInfoPlugin)
    .settings(
        name := "cluster-api",
        libraryDependencies ++= Dependencies.backend,
        commonSettings
    )

//
// Scala Compiler Options
// If this project is only a subproject, add these to a common project setting.
//
scalacOptions in ThisBuild ++= Seq(
  "-target:jvm-1.7",
  "-encoding", "UTF-8",
  "-deprecation", // warning and location for usages of deprecated APIs
  "-feature", // warning and location for usages of features that should be imported explicitly
  "-unchecked", // additional warnings where generated code depends on assumptions
  "-Xlint", // recommended additional warnings
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
  "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
  "-Ywarn-inaccessible",
  "-Ywarn-dead-code"
)

addCommandAlias("seed1", ";project backend;runMain backend.Backend 2551")
addCommandAlias("seed2", ";project backend;runMain backend.Backend 2552")
addCommandAlias("perf2", ";project backend;runMain performance.Performance 2552")
addCommandAlias("front9001", ";project frontend;run 9001")
addCommandAlias("front9002", ";project frontend;run 9002")

fork in run := true