import sbt.Keys.scalaVersion

lazy val commonSettings = Seq(
  organization := "net.jtownson",
  version := "0.1a-SNAPSHOT",
  scalaVersion := "2.12.1",
  scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")
)

lazy val sonatypeSettings = Seq(
  homepage := Some(url("https://bitbucket.org/jtownson/swakka")),
  scmInfo := Some(ScmInfo(url("https://bitbucket.org/jtownson/swakka"), "git@bitbucket.org:jtownson/swakka.git")),
  developers := List(Developer("jtownson", "Jeremy Townson", "jeremy dot townson at gmail dot com", url("https://bitbucket.org/jtownson"))),
  licenses := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))),
  pomIncludeRepository := (_ => false),

  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  }
)


val akka = Seq(
  "com.typesafe.akka" %% "akka-http-core" % "10.0.5",
  "com.typesafe.akka" %% "akka-http" % "10.0.5",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.5",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.5",
  "com.typesafe.akka" %% "akka-http-jackson" % "10.0.5",
  "com.typesafe.akka" %% "akka-http-xml" % "10.0.5")


val shapeless = Seq(
  "com.chuusai" %% "shapeless" % "2.3.2"
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

lazy val library = project
  .settings(
    name := "swakka",
    commonSettings,
    sonatypeSettings,
    libraryDependencies ++=
      akka ++
        scalatest ++
        scalaReflection ++
        shapeless ++
        swaggerAnnotations ++
        jsonPath)

lazy val examples = project
  .settings(
    name := "swakka-examples",
    publishArtifact := false,
    commonSettings)
  .dependsOn(library)

lazy val root = (project in file("."))
  .settings(
    name := "swakka-build",
    publishArtifact := false,
    commonSettings)
  .aggregate(library, examples)