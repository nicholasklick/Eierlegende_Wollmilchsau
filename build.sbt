import AssemblyKeys._  // so that we can make a jar with all the things

assemblySettings


name := "netfauxgo"

version := "0.01"

scalaVersion := "2.9.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.2"

libraryDependencies += "com.typesafe.akka" % "akka-transactor" % "2.0.2"

libraryDependencies += "org.scala-tools" %% "scala-stm" % "0.5"

scalacOptions ++= Seq("-unchecked", "-deprecation")
