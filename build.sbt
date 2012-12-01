import AssemblyKeys._  // so that we can make a jar with all the things

assemblySettings

net.virtualvoid.sbt.graph.Plugin.graphSettings


name := "EierlegendeWollmilchsau"

version := "0.01"

scalaVersion := "2.9.2"

resolvers += ("releases" at "http://oss.sonatype.org/content/repositories/releases")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
	//"org.scala-tools" %% "scala-stm" % "0.6", 
	"com.typesafe.akka" % "akka-actor" % "2.0.4",
	"com.typesafe.akka" % "akka-transactor" % "2.0.4",
	"org.scala-lang"    % "scala-swing" % "2.9.2"
)

scalacOptions ++= Seq("-unchecked", "-deprecation")

//mainClass in (Compile, run) := Some("shapiro.netfauxgo.Bubbler")