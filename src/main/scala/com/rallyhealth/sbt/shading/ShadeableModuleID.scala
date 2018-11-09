package com.rallyhealth.sbt.shading

import sbt.librarymanagement.ModuleID

/**
  * Sugar for defining shaded dependencies.
  *
  * @example {{{
  *              libraryDependencies +=
  *                "com.rallyhealth.core" %% "lib-spartan" % "2.0.0" shaded()
  *          }}}
  */
class ShadeableModuleID(val moduleID: ModuleID) extends AnyVal {

  /**
    * Appends the version string (e.g. "v2") to the artifact name.
    */
  def shaded(): ModuleID = {
    val versionString = Shading.shadeVersionString(moduleID.revision)
    moduleID.withName(s"${moduleID.name}-$versionString")
  }
}
