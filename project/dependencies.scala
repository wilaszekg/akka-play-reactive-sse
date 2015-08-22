import sbt._

object Dependencies {

  object Version {
    val akka = "2.4-M2"
  }

  lazy val frontend = common ++ webjars ++ tests
  lazy val backend = common ++ tests

  val common = Seq(
    "com.typesafe.akka" %% "akka-actor" % Version.akka,
    "com.typesafe.akka" %% "akka-cluster" % Version.akka,
    "com.typesafe.akka" %% "akka-cluster-sharding" % Version.akka,
    "com.typesafe.akka" %% "akka-persistence-experimental" % Version.akka,
    "com.typesafe.akka" %% "akka-cluster-tools" % Version.akka,
    "com.google.guava" % "guava" % "17.0",
//    "org.iq80.leveldb" % "leveldb" % "0.7",
    "com.github.krasserm" %% "akka-persistence-cassandra" % "0.4-SNAPSHOT"
//    "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
  )

  val webjars = Seq(
//    "org.webjars" % "requirejs" % "2.1.11-1",
    "org.webjars" % "underscorejs" % "1.6.0-3",
    "org.webjars" % "jquery" % "1.11.1",
    "org.webjars" % "d3js" % "3.4.9",
    "org.webjars" % "bootstrap" % "3.3.5" exclude ("org.webjars", "jquery")
//    "org.webjars" % "angularjs" % "1.2.16-2" exclude ("org.webjars", "jquery")
  )

  val tests = Seq(
    "org.scalatest" %% "scalatest" % "2.2.4" % "test",
    "org.scalatestplus" %% "play" % "1.2.0" % "test",
    "com.typesafe.akka" %% "akka-testkit" % Version.akka % "test"
  )

}
