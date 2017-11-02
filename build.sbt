import java.net.URL

sbtPlugin := true

name := "shading-sbt-plugin"
organization := "com.rallyhealth.sbt"
organizationName := "Rally Health"

scalacOptions ++= Seq("-Xfatal-warnings", "-Xlint", "-feature")
licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))

bintrayOrganization := Some("rallyhealth")
bintrayRepository := "sbt-plugins"

publishMavenStyle := false

sources in doc := Seq.empty

publishArtifact in packageDoc := false

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)
