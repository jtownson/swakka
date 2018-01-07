/*
 * Copyright 2017 Jeremy Townson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt.Keys.scalaVersion

lazy val commonSettings = Seq(
  organization := "net.jtownson",
  version := "0.1a-SNAPSHOT",

//  scalaOrganization := "org.typelevel",
//  scalaVersion := "2.12.4-bin-typelevel-4",

  scalaVersion := "2.12.4",
  scalacOptions := Seq(/*"-Xlog-implicits",*/ "-Ypartial-unification", "-unchecked", "-deprecation", "-feature", "-language:implicitConversions")
)

lazy val sonatypeCredentials = (sys.env.get("SONATYPE_USER"), sys.env.get("SONATYPE_PASSWORD")) match {
  case (Some(user), Some(password)) =>
    Credentials("Sonatype Nexus Repository Manager",
      "oss.sonatype.org",
      user, password)
  case _ => Credentials.toDirect(Credentials(Path.userHome / ".sbt" / "0.13" / ".sonatype_credentials"))
}

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
  },

  credentials += sonatypeCredentials
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
  "org.scala-lang" % "scala-reflect" % "2.12.4"
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