import sbt.complete.DefaultParsers._

scalaVersion := "2.11.8"

organization := "com.rallyhealth.test.scripted"

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

version := "0.0.1"

enablePlugins(ShadingPlugin)

lazy val assertName = inputKey[Unit]("Asserts that the name is a specific value.")

assertName := {
  val actual = name.value
  val expected = spaceDelimited("<arg>").parsed.head

  assert(expected == actual, s"expected: $expected actual: $actual")
}
