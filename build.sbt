name := "swakka"

version := "1.0"

//scalaVersion := "2.11.8"
scalaVersion := "2.12.1"
//scalaOrganization in ThisBuild := "org.typelevel"

val akka = Seq(
  "com.typesafe.akka" %% "akka-http-core" % "10.0.5",
  "com.typesafe.akka" %% "akka-http" % "10.0.5",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.5",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.5",
  "com.typesafe.akka" %% "akka-http-jackson" % "10.0.5",
  "com.typesafe.akka" %% "akka-http-xml" % "10.0.5")

val sprayJsonShapeless = Seq(
  "com.github.fommil" %% "spray-json-shapeless" % "1.3.0"
)

val scalatest = Seq(
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test"
)

//val autoschema = Seq(
//  "com.sauldhernandez" %% "autoschema" % "1.0.3"
//)

libraryDependencies := akka ++ scalatest ++ /*autoschema ++ */ sprayJsonShapeless