package com.rallyhealth.sbt.shading

class ProjectNameUnshadedException(shadedVersion: String, projectName: String) extends Exception(s"Project $projectName is not shaded with $shadedVersion")
