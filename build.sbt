import sbt.Keys.scalaVersion

name := "swakka"

version := "1.0"

scalaVersion := "2.12.1"
//scalaOrganization in ThisBuild := "org.typelevel"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

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
  "org.scalactic" %% "scalactic" % "3.0.1" % "test",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % "test"
)

val scalaReflection = Seq(
  "org.scala-lang" % "scala-reflect" % "2.12.1"
)

val swaggerAnnotations = Seq(
  "io.swagger" % "swagger-annotations" % "1.5.13"
)

val jsonPath = Seq(
  "com.jayway.jsonpath" % "json-path" % "2.2.0"
)

libraryDependencies := akka ++ scalatest ++ scalaReflection ++ sprayJsonShapeless ++ swaggerAnnotations ++ jsonPath
