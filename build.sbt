name := "ucu-scala-final-project"

version := "0.1"

scalaVersion := "2.13.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.6.10",
  "com.typesafe.akka" %% "akka-stream" % "2.6.10",
  "com.typesafe.akka" %% "akka-http" % "10.2.1",
  "com.lightbend.akka" %% "akka-stream-alpakka-mongodb" % "2.0.2",
  "com.typesafe.akka" %% "akka-serialization-jackson" % "2.6.10",

  "org.twitter4j" % "twitter4j-core" % "4.0.5",
  "org.twitter4j" % "twitter4j-stream" % "4.0.5",
)
