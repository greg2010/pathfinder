import sbt.{Credentials, Path}

name := "Pathfinder"

organization := "org.red"

version := "0.1"

scalaVersion := "2.12.4"

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

resolvers += "Sonatype OSS Snapshots" at
  "https://oss.sonatype.org/content/repositories/releases"

val circeVersion = "0.8.0"
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats" % "0.9.0",
  "com.typesafe" % "config" % "1.3.1",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.github.pukkaone" % "logback-gelf" % "1.1.10",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "com.roundeights" %% "hasher" % "1.2.0",
  "moe.pizza" %% "eveapi" % "0.58-SNAPSHOT",
  "org.red" %% "reddb" % "1.0.12-SNAPSHOT",
  "org.red" %% "iris" % "0.0.18-SNAPSHOT",
  "net.troja.eve" % "eve-esi" % "1.0.0",
  "org.glassfish.jersey.core" % "jersey-common" % "2.25.1",
  "io.monix" %% "monix" % "2.3.0",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.storm-enroute" %% "scalameter-core" % "0.8.2",
  "de.ummels" %% "scala-prioritymap" % "1.0.0")