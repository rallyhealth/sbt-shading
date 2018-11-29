package com.rallyhealth.sbt.shading

import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object ShadingPlugin extends AutoPlugin {

  override def requires: Plugins = {
    /**
      * We hook test & publish which can only happen after the Defaults are set by the JvmPlugin.
      * Otherwise, our hooks will get overridden.
      *
      * @see http://stackoverflow.com/a/25251194
      */
    JvmPlugin
  }

  /**
    * These are imported into the build.sbt's scope automatically.
    */
  object autoImport extends ShadingImplicits {

    lazy val shadingVersionString = settingKey[String]("A version string like 'v2' that all packages and directories must include.")
    lazy val shadingCheck = taskKey[Unit]("Runs all checks that the project is fully shaded.")
    lazy val shadingCheckFiles = taskKey[Unit]("Checks that all scala directories/packages include the shaded version.")
    lazy val shadingCheckArtifactName = taskKey[Unit]("Checks that the project/artifact name includes the shaded version.")
    lazy val shadingNameShader = settingKey[String => String]("Adds the shading version string to the name of the artifact.")
  }

  import autoImport._

  /**
    * These get applied to any project that calls: .enablePlugins(ShadingPlugin)
    */
  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    (publish in Compile) := (publish in Compile).dependsOn(shadingCheck).value,
    (test in Test) := (test in Test).dependsOn(shadingCheck).value,
    shadingCheck := {
      shadingCheckArtifactName.value
      shadingCheckFiles.value
    },
    shadingVersionString := Shading.shadeVersionString(version.value),
    shadingCheckArtifactName := {
      if (!name.value.contains(shadingVersionString.value)) {
        throw new ProjectNameUnshadedException(shadingVersionString.value, name.value)
      }
    },
    shadingNameShader := {
      // Making use of name.value directly results in a circular dependency.
      (name: String) => s"${name}-${shadingVersionString.value}"
    },
    name := shadingNameShader.value(name.value),
    sources in shadingCheckFiles := ((scalaSource in Compile).value ** "*.scala").get,
    shadingCheckFiles := {
      val scalaFiles = (sources in shadingCheckFiles).value

      val errors = scalaFiles.foldLeft(Seq.newBuilder[FileShadingErrors]) { (builder, file) =>
        builder ++= Shading.validate(shadingVersionString.value, file)
      }.result()

      if (errors.nonEmpty) {
        throw new FileShadingErrorsException(shadingVersionString.value, errors.map(_.relativeTo(baseDirectory.value)))
      }
    }
  )
}
