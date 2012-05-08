name := "netfauxgo"

version := "0.01"

scalaVersion := "2.9.2"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.1"

libraryDependencies += "com.typesafe.akka" % "akka-transactor" % "2.0.1"

libraryDependencies += "org.scala-tools" %% "scala-stm" % "0.5"
