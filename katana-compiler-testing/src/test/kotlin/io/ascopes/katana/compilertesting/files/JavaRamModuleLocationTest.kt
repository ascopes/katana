@file:Suppress("UsePropertyAccessSyntax")

package io.ascopes.katana.compilertesting.files

import io.ascopes.katana.compilertesting.Each
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import java.nio.file.Path
import java.util.UUID
import javax.tools.JavaFileManager.Location
import javax.tools.StandardLocation
import javax.tools.StandardLocation.CLASS_PATH

class JavaRamModuleLocationTest {
  @Test
  fun `getName() returns the path as a name`() {
    val path = Path.of(UUID.randomUUID().toString(), UUID.randomUUID().toString())
    val location = JavaRamModuleLocation(
        CLASS_PATH,
        path,
        "my.modulename.here"
    )

    assertEquals(path.toString(), location.getName())
  }

  @Each.InputOutputLocation
  @ParameterizedTest
  fun `isOutputLocation() returns if the parent is an output location`(parentLocation: Location) {
    val location = JavaRamModuleLocation(
        parentLocation,
        Path.of("/foo/bar"),
        "my.modulename.here"
    )

    assertEquals(parentLocation.isOutputLocation(), location.isOutputLocation())
  }

  @Each.ModuleOrientedLocation
  @ParameterizedTest
  fun `isModuleOrientedLocation() always returns false`(parentLocation: Location) {
    val location = JavaRamModuleLocation(
        parentLocation,
        Path.of("/foo/bar"),
        "my.modulename.here"
    )

    assertFalse(location.isModuleOrientedLocation())
  }

  @Nested
  inner class EqualsTest {
    @Test
    fun `equal to itself`() {
      val location = JavaRamModuleLocation(
          StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH,
          Path.of("/foo/bar"),
          "my.modulename.here"
      )

      assertEquals(location, location)
    }

    @Test
    fun `equal to other instances with the same location and module name`() {
      val location1 = JavaRamModuleLocation(
          StandardLocation.SOURCE_OUTPUT,
          Path.of("/foo/bar"),
          "my.modulename.here"
      )

      val location2 = JavaRamModuleLocation(
          StandardLocation.SOURCE_OUTPUT,
          Path.of("/foo/blep"),
          "my.modulename.here"
      )

      assertEquals(location1, location2)
    }

    @Test
    fun `not equal if the parent location is different`() {
      val location1 = JavaRamModuleLocation(
          StandardLocation.SOURCE_OUTPUT,
          Path.of("/foo/bar"),
          "my.modulename.here"
      )

      val location2 = JavaRamModuleLocation(
          StandardLocation.MODULE_PATH,
          Path.of("/foo/bar"),
          "my.modulename.here"
      )

      assertNotEquals(location1, location2)
    }

    @Test
    fun `not equal if the module name is different`() {
      val location1 = JavaRamModuleLocation(
          StandardLocation.MODULE_PATH,
          Path.of("/foo/bar"),
          "foo.modulename"
      )

      val location2 = JavaRamModuleLocation(
          StandardLocation.MODULE_PATH,
          Path.of("/foo/bar"),
          "bar.modulename"
      )

      assertNotEquals(location1, location2)
    }

    @Test
    fun `not equal if not an instance of JavaRamModuleLocation`() {
      val location1 = JavaRamModuleLocation(
          StandardLocation.SOURCE_OUTPUT,
          Path.of("/foo/bar"),
          "my.modulename.here"
      )

      val location2 = StandardLocation.SOURCE_OUTPUT

      assertNotEquals(location1, location2)
    }
  }

  @Nested
  inner class HashCodeTest {
    @Test
    fun `hash codes are equal for the same instance`() {
      val location = JavaRamModuleLocation(
          StandardLocation.PATCH_MODULE_PATH,
          Path.of("/foo/bar"),
          "my.modulename.here"
      )

      assertEquals(location.hashCode(), location.hashCode())
    }

    @Test
    fun `hash codes are equal for different instances with the same values`() {
      val location1 = JavaRamModuleLocation(
          StandardLocation.PATCH_MODULE_PATH,
          Path.of("/foo/bar"),
          "my.modulename.here"
      )

      val location2 = JavaRamModuleLocation(
          StandardLocation.PATCH_MODULE_PATH,
          Path.of("/foo/bar"),
          "my.modulename.here"
      )

      assertEquals(location1.hashCode(), location2.hashCode())
    }

    @Test
    fun `hash codes differ for different module names`() {
      // XXX: could this collide and fail unnecessarily?
      val location1 = JavaRamModuleLocation(
          StandardLocation.PATCH_MODULE_PATH,
          Path.of("/foo/bar"),
          "my.modulename.here"
      )

      val location2 = JavaRamModuleLocation(
          StandardLocation.PATCH_MODULE_PATH,
          Path.of("/foo/bar"),
          "my.modulename.there"
      )

      assertNotEquals(location1.hashCode(), location2.hashCode())
    }

    @Test
    fun `hash codes differ for different locations`() {
      // XXX: could this collide and fail unnecessarily?
      val location1 = JavaRamModuleLocation(
          StandardLocation.PLATFORM_CLASS_PATH,
          Path.of("/foo/bar"),
          "my.modulename.here"
      )

      val location2 = JavaRamModuleLocation(
          StandardLocation.MODULE_PATH,
          Path.of("/foo/bar"),
          "my.modulename.here"
      )

      assertNotEquals(location1.hashCode(), location2.hashCode())
    }
  }
}