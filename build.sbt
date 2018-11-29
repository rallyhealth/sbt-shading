import sbt.Keys.crossSbtVersions
import sbt.plugins.SbtPlugin

sbtPlugin := true

name := "sbt-shading"
organizationName := "Rally Health"
organization := "com.rallyhealth.sbt"
licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))

// SbtPlugin requires sbt 1.2.0+
// See: https://developer.lightbend.com/blog/2018-07-02-sbt-1-2-0/#sbtplugin-for-plugin-development
enablePlugins(SbtPlugin)

scalacOptions ++= Seq("-Xfatal-warnings", "-Xlint", "-feature")

bintrayOrganization := Some("rallyhealth")
bintrayRepository := "sbt-plugins"

sources in doc := Seq.empty

publishArtifact in packageDoc := false

// Shims for sbt 0.13
addSbtPlugin("com.dwijnand" % "sbt-compat" % "1.2.6")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

// https://www.scala-sbt.org/1.0/docs/Cross-Build-Plugins.html
scalaVersion := "2.12.6"
sbtVersion in Global := "1.2.3"
crossSbtVersions := List("0.13.17", "1.2.3")
