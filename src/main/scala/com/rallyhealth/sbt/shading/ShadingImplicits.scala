package com.rallyhealth.sbt.shading

import sbt.ModuleID

object ShadingImplicits extends ShadingImplicits

trait ShadingImplicits {
  import scala.language.implicitConversions

  implicit def toShadeableModuleID(value: ModuleID) = new ShadeableModuleID(value)
}
