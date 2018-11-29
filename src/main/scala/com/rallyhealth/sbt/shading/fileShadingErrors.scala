package com.rallyhealth.sbt.shading

import sbt._

case class FileShadingErrors(file: File, errors: Seq[FileShadingError]) {

  def relativeTo(base: File): FileShadingErrors = copy(file = file.relativeTo(base).getOrElse(file))

  override def toString: String = s"$file: ${errors.mkString(", ")}"
}

class FileShadingErrorsException(shadedVersion: String, errors: Seq[FileShadingErrors])
  extends Exception(
    errors.mkString(s"""Expected name shaded version "$shadedVersion" for:\n""".stripMargin, "\n", "")
  )

sealed trait FileShadingError

/**
  * Package declaration does not contain the shaded version.
  */
case class PackageNameUnshaded(wrongName: String) extends FileShadingError

/**
  * Package declaration could not be found in the file.
  *
  * There is no reason to do this in libraries.
  */
case object PackageNameMissing extends FileShadingError

/**
  * Directory structure does not contain the shaded version.
  */
case object DirectoryUnshaded extends FileShadingError
