package com.rallyhealth.sbt.shading

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.FlatSpec
import sbt._

class ShadeableModuleIDSpec extends FlatSpec with TypeCheckedTripleEquals {

  "A ModuleID" should "be shaded" in {
    val moduleID = "org" % "name" % "1.0.0"
    val shadedModuleID = new ShadeableModuleID(moduleID).shaded()

    assert(shadedModuleID.organization == "org")
    assert(shadedModuleID.name == "name-v1")
    assert(shadedModuleID.revision == "1.0.0")
  }
}
