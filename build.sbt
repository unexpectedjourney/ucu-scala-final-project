name := "ucu-scala-final-project"

version := "0.1"

scalaVersion := "2.13.4"

lazy val twitter = (project in file("."))
  .settings(
    mainClass in assembly := Some("ua.edu.ucu.TwitterApp"),
    assemblyJarName in assembly := "twitter.jar",
    inThisBuild(List(
      organization := "ua.edu.ucu",
    )),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.6.10",
      "com.typesafe.akka" %% "akka-stream" % "2.6.10",
      "com.typesafe.akka" %% "akka-http" % "10.2.1",
      "com.lightbend.akka" %% "akka-stream-alpakka-mongodb" % "2.0.2",
      "com.typesafe.akka" %% "akka-serialization-jackson" % "2.6.10",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.2",
      "org.twitter4j" % "twitter4j-core" % "4.0.5",
      "org.twitter4j" % "twitter4j-stream" % "4.0.5",
      "org.mongodb.scala" %% "mongo-scala-driver" % "2.9.0",
      "com.typesafe.akka" %% "akka-actor-typed" % "2.6.10",
      "org.slf4j" % "slf4j-simple" % "1.7.12",
    ),
    name := "twitter"
  )


lazy val news = (project in file("."))
  .settings(
    mainClass in assembly := Some("ua.edu.ucu.NewsApp"),
    assemblyJarName in assembly := "news.jar",
    inThisBuild(List(
      organization := "ua.edu.ucu",
    )),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.6.10",
      "com.typesafe.akka" %% "akka-stream" % "2.6.10",
      "com.typesafe.akka" %% "akka-http" % "10.2.1",
      "com.lightbend.akka" %% "akka-stream-alpakka-mongodb" % "2.0.2",
      "com.typesafe.akka" %% "akka-serialization-jackson" % "2.6.10",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.2",

      "org.twitter4j" % "twitter4j-core" % "4.0.5",
      "org.twitter4j" % "twitter4j-stream" % "4.0.5",
      "org.mongodb.scala" %% "mongo-scala-driver" % "2.9.0",
      "org.slf4j" % "slf4j-simple" % "1.7.12",
    ),
    name := "news"
  )


lazy val mainApplication = (project in file("."))
  .settings(
    mainClass in assembly := Some("ua.edu.ucu.Application"),
    assemblyJarName in assembly := "mainApplication.jar",
    inThisBuild(List(
      organization := "ua.edu.ucu",
    )),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.6.10",
      "com.typesafe.akka" %% "akka-stream" % "2.6.10",
      "com.typesafe.akka" %% "akka-http" % "10.2.1",
      "com.lightbend.akka" %% "akka-stream-alpakka-mongodb" % "2.0.2",
      "com.typesafe.akka" %% "akka-serialization-jackson" % "2.6.10",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.2",

      "org.twitter4j" % "twitter4j-core" % "4.0.5",
      "org.twitter4j" % "twitter4j-stream" % "4.0.5",
      "org.mongodb.scala" %% "mongo-scala-driver" % "2.9.0",
      "com.typesafe.akka" %% "akka-actor-typed" % "2.6.10",
      "org.slf4j" % "slf4j-simple" % "1.7.12",
    ),
    name := "mainApplication"
  )


assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case x => MergeStrategy.first
}
