package io.ascopes.katana.compilertesting

import java.nio.charset.StandardCharsets
import javax.lang.model.SourceVersion
import javax.tools.JavaFileManager.Location
import org.junit.jupiter.params.provider.MethodSource

/**
 * Various re-usable fixtures for parameterized tests.
 */
object Each {
  @MethodSource("io.ascopes.katana.compilertesting.Each#javaVersions")
  annotation class JavaVersion

  @JvmStatic
  fun javaVersions() = SourceVersion
      .values()
      .filter { it >= SourceVersion.RELEASE_11 }
      .toList()

  @MethodSource("io.ascopes.katana.compilertesting.Each#inputOutputLocations")
  annotation class InputOutputLocation

  @JvmStatic
  fun inputOutputLocations() = listOf(
      object : Location {
        override fun getName() = "input location"
        override fun isOutputLocation() = false
        override fun toString() = this.name
      },
      object : Location {
        override fun getName() = "output location"
        override fun isOutputLocation() = true
        override fun toString() = this.name
      }
  )

  @MethodSource("io.ascopes.katana.compilertesting.Each#moduleOrientedLocations")
  annotation class ModuleOrientedLocation

  @JvmStatic
  fun moduleOrientedLocations() = listOf(
      object : Location {
        override fun getName() = "non-module-oriented location"
        override fun isOutputLocation() = false
        override fun isModuleOrientedLocation() = false
        override fun toString() = this.name
      },
      object : Location {
        override fun getName() = "module-oriented location"
        override fun isOutputLocation() = true
        override fun isModuleOrientedLocation() = true
        override fun toString() = this.name
      }
  )

  @MethodSource("io.ascopes.katana.compilertesting.Each#standardCharsets")
  annotation class StandardCharset

  @JvmStatic
  fun standardCharsets() = listOf(
      StandardCharsets.ISO_8859_1,
      StandardCharsets.US_ASCII,
      StandardCharsets.UTF_16BE,
      StandardCharsets.UTF_16BE,
      StandardCharsets.UTF_16,
      StandardCharsets.UTF_8,
  )
}