import sbt.{Credentials, Path}

name := "Pathfinder"

organization := "org.red"

version := "0.1"

scalaVersion := "2.12.4"


credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

val circeVersion = "0.8.0"
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats" % "0.9.0",
  "com.typesafe" % "config" % "1.3.1",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.github.pukkaone" % "logback-gelf" % "1.1.10",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "com.roundeights" %% "hasher" % "1.2.0",
  "moe.pizza" %% "eveapi" % "0.58-SNAPSHOT",
  "org.red" %% "reddb" % "1.0.10-SNAPSHOT",
  "org.red" %% "iris" % "0.0.18-SNAPSHOT",
  "net.troja.eve" % "eve-esi" % "1.0.0",
  "org.glassfish.jersey.core" % "jersey-common" % "2.25.1",
  "io.monix" %% "monix" % "2.3.0",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "org.scala-graph" %% "graph-core" % "1.12.1")