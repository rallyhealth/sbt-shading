scalaVersion := "2.11.8"

organization := "com.rallyhealth.test.scripted"

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

version := "0.0.1"

enablePlugins(ShadingPlugin)
