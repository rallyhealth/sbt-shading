package com.rallyhealth.sbt.shading

import java.io.File

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{FunSpec, Matchers, OptionValues}
import sbt._

class ShadingTest extends FunSpec with Matchers with TypeCheckedTripleEquals with OptionValues {

  locally {
    val dir = IO.createTemporaryDirectory

    def createFile(name: String)(contents: String): File = {
      val file = dir / name
      IO.write(file, contents)
      file
    }

    describe("packages") {
      it("approves shaded packages") {
        val result = Shading.validatePackageName(
          "v2", createFile("GoodPackaged.scala") {
            """
              |package com.rallyhealth.v2
              |
              |class GoodPackaged
            """.stripMargin
          }
        )
        assert(result === None)
      }

      it("fails unshaded packages") {
        val result = Shading.validatePackageName(
          "v2", createFile("BadUnshadedPackage.scala") {
            """
              |package com.rallyhealth.unshaded
              |
              |class BadUnshadedPackage
            """.stripMargin
          }
        )
        assert(result === Some(PackageNameUnshaded("com.rallyhealth.unshaded")))
      }

      it("fails missing packages") {
        val result = Shading.validatePackageName(
          "v2", createFile("Unpackaged.scala") {
            """
              |class Unpackaged
            """.stripMargin
          }
        )
        assert(result === Some(PackageNameMissing))
      }
    }

    describe("package objects") {
      it("approves package objects with version in package object") {
        val result = Shading.validatePackageName(
          "v2", createFile("package.scala") {
            """
              |package com.rallyhealth.whatever
              |
              |package object v2
            """.stripMargin
          }
        )
        assert(result === None)
      }

      it("approves package objects with version in parent package") {
        val result = Shading.validatePackageName(
          "v2", createFile("package.scala") {
            """
              |package com.rallyhealth.whatever.v2
              |
              |package object whatever
            """.stripMargin
          }
        )
        assert(result === None)
      }

      it("fails unshaded package objects") {
        val result = Shading.validatePackageName(
          "v2", createFile("package.scala") {
            """
              |package com.rallyhealth.whatever
              |
              |package object notVersioned
            """.stripMargin
          }
        )
        assert(result === Some(PackageNameUnshaded("com.rallyhealth.whatever.notVersioned")))
      }
    }

    describe("directories") {
      it("approves shaded directories") {
        val file = dir / "com" / "rallyhealth" / "v2" / "GoodShaded.scala"
        file.mkdirs()
        file.createNewFile()

        val result = Shading.validateDirName("v2", file)
        assert(result === None)
      }

      it("fails unversioned directories") {
        val file = dir / "BadUnshaded.scala"
        file.createNewFile()
        val result = Shading.validateDirName("v2", file)
        assert(result === Some(DirectoryUnshaded))
      }
    }

    describe("validate") {
      it("should validate all the things") {
        val file = dir / "com" / "rallyhealth" / "bad" / "Bad.scala"
        IO.write(
          file,
          """
            |package com.rallyhealth.bad
            |
            |trait Unshaded
          """.stripMargin
        )
        val result = Shading.validate("v2", file)
        result.value.errors should contain allOf (DirectoryUnshaded, PackageNameUnshaded("com.rallyhealth.bad"))
      }
    }
  }
}
