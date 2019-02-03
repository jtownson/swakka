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
  version := "0.53",

//  scalaOrganization := "org.typelevel",
//  scalaVersion := "2.12.4-bin-typelevel-4",

  autoCompilerPlugins := true,
//  addCompilerPlugin("io.tryp" % "splain" % "0.3.1" cross CrossVersion.patch),

  scalaVersion := "2.12.7",
  scalacOptions := Seq(/*"-Xlog-implicits",*/ "-Ypartial-unification", "-unchecked", "-deprecation", "-feature", "-language:implicitConversions")
)

val sonatypeCredentialsFile = Path.userHome / ".sbt" / "0.13" / ".sonatype_credentials"

lazy val sonatypeCredentials = (sys.env.get("SONATYPE_USER"), sys.env.get("SONATYPE_PASSWORD")) match {
  case (Some(user), Some(password)) =>
    Credentials("Sonatype Nexus Repository Manager",
      "oss.sonatype.org",
      user, password)
  case _ if sonatypeCredentialsFile.isFile => Credentials.toDirect(Credentials(sonatypeCredentialsFile))
  case _ => Credentials("Sonatype Nexus Repository Manager",
      "oss.sonatype.org",
      "guest", "")

}

lazy val sonatypeSettings = Seq(
  homepage := Some(url("https://github.com/jtownson/swakka")),
  scmInfo := Some(ScmInfo(url("https://github.com/jtownson/swakka"), "git@github.com:jtownson/swakka.git")),
  developers := List(Developer("jtownson", "Jeremy Townson", "jeremy dot townson at gmail dot com", url("https://github.com/jtownson"))),
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

val akkaHttpVersion = "10.1.4"
val akka = Seq(
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-jackson" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion)


val shapeless = Seq(
  "com.chuusai" %% "shapeless" % "2.3.2"
)

val scalatest = Seq(
  "org.scalactic" %% "scalactic" % "3.0.1" % "test",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % "test"
)

val scalaReflection_2_12 = Seq(
  "org.scala-lang" % "scala-reflect" % "2.12.8"
)

val scalaReflection_2_11 = Seq(
  "org.scala-lang" % "scala-reflect" % "2.11.12"
)

val swaggerAnnotations = Seq(
  "io.swagger" % "swagger-annotations" % "1.5.13"
)

val jsonPath = Seq(
  "com.jayway.jsonpath" % "json-path" % "2.2.0"
)

val enumeratum = Seq(
  "com.beachape" %% "enumeratum" % "1.5.13"
)

lazy val library = project
  .settings(
    name := "swakka",
    commonSettings,
    sonatypeSettings,
    libraryDependencies ++=
      akka ++
        scalatest ++
        shapeless ++
        swaggerAnnotations ++
        jsonPath ++
        enumeratum)
  .cross

lazy val library_2_12 = library("2.12.8").settings(
  libraryDependencies ++= scalaReflection_2_12,
  name := "swakka"
)

lazy val library_2_11 = library("2.11.12").settings(
  libraryDependencies ++= scalaReflection_2_11,
  name := "swakka"
)

lazy val examples = project
  .settings(
    name := "swakka-examples",
    publishArtifact := false,
    commonSettings)
  .dependsOn(library_2_12)

lazy val root = (project in file("."))
  .settings(
    name := "swakka-build",
    publishArtifact := false,
    commonSettings)
  .aggregate(library_2_11, library_2_12, examples)