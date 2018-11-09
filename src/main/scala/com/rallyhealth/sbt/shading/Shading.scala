package com.rallyhealth.sbt.shading

import sbt._

import scala.io.Source

object Shading {

  def validate(shadedVersion: String, f: File): Option[FileShadingErrors] = {
    val errors = validateDirName(shadedVersion, f) ++ validatePackageName(shadedVersion, f)

    if (errors.nonEmpty) {
      Some(FileShadingErrors(f, errors.toSeq))
    } else {
      None
    }
  }

  /**
    * Checks the file's parent directories to make sure there is one that exactly matches the shadedVersion.
    *
    * For shadedVersion "v2":
    *
    * Valid: "src/main/scala/com/rallyhealth/whatever/v2/File.scala"
    *
    * Invalid: "src/main/scala/com/rallyhealth/whatever/File.scala" // because shaded version is missing.
    * Invalid: "src/main/scala/com/rallyhealth/v1/whatever/File.scala" // because shaded version should be "v2", not "v1"
    */
  def validateDirName(shadedVersion: String, f: File): Option[FileShadingError] = {
    if (parentDirs(f).map(_.getName).contains(shadedVersion)) {
      None
    } else {
      Some(DirectoryUnshaded)
    }
  }

  /**
    * Scans the file contents for the package definition and verifies that it includes the shadedVersion.
    *
    * For shadedVersion "v2":
    *
    * Valid: "com.rallyhealth.whatever.v2"
    *
    * Invalid: "com.rallyhealth.whatever" // because shaded version is missing.
    * Invalid: "com.rallyhealth.v1.whatever" // because shaded version should be "v2", not "v1"
    *
    */
  def validatePackageName(shadedVersion: String, f: File): Option[FileShadingError] = {
    extractPackage(f) match {
      case None => Some(PackageNameMissing) // not okay for shared libs.
      case Some(p) if p.split('.').contains(shadedVersion) => None
      case Some(p) if f.getName == "package.scala" =>

        /**
          * Special treatment for package objects.
          *
          * Valid example of com/rallyhealth/whatever/v2/package.scala:
          *
          * {{{
          * package com.rallyhealth.whatever // normally invalid.
          *
          * package object v2 { // redeemed!
          *   // ...
          * }
          * }}}
          */
        extractPackageObjectNames(f).filterNot(_ contains shadedVersion).toList match {
          case Nil => None
          case firstBaddie :: _ => Some(PackageNameUnshaded(p + "." + firstBaddie))
        }
      case Some(p) => Some(PackageNameUnshaded(p))
    }
  }

  /**
    * Reads the file until it finds a package declaration and extracts the value.
    */
  private def extractPackage(scalaFile: File): Option[String] = {
    Source.fromFile(scalaFile)
      .getLines()
      .collectFirst {
        case PackageRegex(p) => p
      }
  }

  /**
    * Extracts the package object name from a package.scala.
    *
    * For example, extracts "v2" from:
    * {{{
    * package com.rallyhealth.whatever
    *
    * package object v2
    * }}}
    */
  private def extractPackageObjectNames(packageObject: File) = {
    Source.fromFile(packageObject)
      .getLines()
      .collect {
        case PackageObjectRegex(packageName) => packageName
      }
  }

  /**
    * Gets a File pointing to each ancestor from the file's parent to root.
    */
  private def parentDirs(f: File): Stream[File] = {
    Option(f.getParentFile) match {
      case None => Stream.empty
      case Some(f) => f #:: parentDirs(f)
    }
  }

  def shadeVersionString(version: String): String = "v" + version.takeWhile(_ != '.')

  /**
    * Naively ignores nested packages and perhaps other things that are possible in scala.
    *
    * Let's cross that bridge when we come to it.
    */
  private val PackageRegex = "\\s*package\\s+([^\\s]+)\\s*".r
  private val PackageObjectRegex = "\\s*package\\s+object\\s+([^\\s]+).*".r
}
