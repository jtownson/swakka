name := "swakka"

version := "1.0"

scalaVersion := "2.11.8"

val akka = Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.11",
  "com.typesafe.akka" %% "akka-stream" % "2.4.11",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.11",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.11",
  "com.typesafe.akka" %% "akka-http-testkit" % "2.4.11" % "test"
)

val sprayJsonShapeless = Seq(
  "com.github.fommil" %% "spray-json-shapeless" % "1.3.0"
)

val scalatest = Seq(
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

//val autoschema = Seq(
//  "com.sauldhernandez" %% "autoschema" % "1.0.3"
//)

libraryDependencies := akka ++ scalatest ++ /*autoschema ++ */ sprayJsonShapeless