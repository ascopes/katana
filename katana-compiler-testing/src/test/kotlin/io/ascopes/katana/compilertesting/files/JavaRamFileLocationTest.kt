@file:Suppress("UsePropertyAccessSyntax")

package io.ascopes.katana.compilertesting.files

import io.ascopes.katana.compilertesting.Each
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import java.nio.file.Path
import java.util.UUID
import javax.tools.JavaFileManager.Location
import javax.tools.StandardLocation
import javax.tools.StandardLocation.CLASS_PATH

class JavaRamFileLocationTest {
  @Test
  fun `getName() returns the path as a name`() {
    val path = Path.of(UUID.randomUUID().toString(), UUID.randomUUID().toString())
    val location = JavaRamFileLocation(CLASS_PATH, path)

    assertEquals(path.toString(), location.getName())
  }

  @Each.InputOutputLocation
  @ParameterizedTest
  fun `isOutputLocation() returns if the parent is an output location`(parentLocation: Location) {
    val location = JavaRamFileLocation(parentLocation, Path.of("/foo/bar"))

    assertEquals(parentLocation.isOutputLocation(), location.isOutputLocation())
  }

  @Each.ModuleOrientedLocation
  @ParameterizedTest
  fun `isModuleOrientedLocation() returns if the parent is module oriented`(
      parentLocation: Location
  ) {
    val location = JavaRamFileLocation(parentLocation, Path.of("/foo/bar"))

    assertEquals(parentLocation.isModuleOrientedLocation(), location.isModuleOrientedLocation())
  }

  @Nested
  inner class EqualsTest {
    @Test
    fun `equal to itself`() {
      val location = JavaRamFileLocation(
          StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH,
          Path.of("/foo/bar")
      )

      assertEquals(location, location)
    }

    @Test
    fun `equal to other instances with the same base location`() {
      val location1 = JavaRamFileLocation(
          StandardLocation.SOURCE_OUTPUT,
          Path.of("/foo/bar")
      )

      val location2 = JavaRamFileLocation(
          StandardLocation.SOURCE_OUTPUT,
          Path.of("/foo/blep"),
      )

      assertEquals(location1, location2)
    }

    @Test
    fun `equal to the base location`() {
      val location = JavaRamFileLocation(
          StandardLocation.SOURCE_OUTPUT,
          Path.of("/foo/bar")
      )

      assertEquals(location, StandardLocation.SOURCE_OUTPUT)
    }

    @Test
    fun `not equal if the base location is different`() {
      val location1 = JavaRamFileLocation(
          StandardLocation.SOURCE_OUTPUT,
          Path.of("/foo/bar")
      )

      val location2 = JavaRamFileLocation(
          StandardLocation.MODULE_PATH,
          Path.of("/foo/bar")
      )

      Assertions.assertNotEquals(location1, location2)
    }
  }

  @Nested
  inner class HashCodeTest {
    @Test
    fun `hash codes are equal for the same instance`() {
      val location = JavaRamFileLocation(
          StandardLocation.PATCH_MODULE_PATH,
          Path.of("/foo/bar")
      )

      assertEquals(location.hashCode(), location.hashCode())
    }

    @Test
    fun `hash codes are equal for different instances with the same base location`() {
      val location1 = JavaRamFileLocation(
          StandardLocation.PATCH_MODULE_PATH,
          Path.of("/foo/bar/baz")
      )

      val location2 = JavaRamFileLocation(
          StandardLocation.PATCH_MODULE_PATH,
          Path.of("/foo/bar/bork")
      )

      assertEquals(location1.hashCode(), location2.hashCode())
    }

    @Test
    fun `hash codes differ for different locations`() {
      // XXX: could this collide and fail unnecessarily?
      val location1 = JavaRamFileLocation(StandardLocation.SOURCE_OUTPUT, Path.of("/foo/bar"))
      val location2 = JavaRamFileLocation(StandardLocation.MODULE_PATH, Path.of("/foo/bar"))

      Assertions.assertNotEquals(location1.hashCode(), location2.hashCode())
    }
  }
}