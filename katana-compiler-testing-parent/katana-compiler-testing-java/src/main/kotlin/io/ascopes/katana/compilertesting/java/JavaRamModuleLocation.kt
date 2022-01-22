package io.ascopes.katana.compilertesting.java

import java.nio.file.Path
import java.util.Objects
import javax.tools.JavaFileManager.Location

/**
 * Input/output location that occurs in-memory for a module.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 * @param parentLocation the parent location that this location occurs within.
 * @param path the absolute path of the location.
 * @param moduleName the name of the module.
 */
data class JavaRamModuleLocation internal constructor(
    val parentLocation: Location,
    override val path: Path,
    val moduleName: String
) : JavaRamLocation {
  /**
   * @return the name of the file.
   */
  override fun getName() = this.path.toString()

  /**
   * @return `true` if the parent location is an output location, or `false` if it is not.
   */
  override fun isOutputLocation() = this.parentLocation.isOutputLocation

  /**
   * @return `false`, always.
   */
  override fun isModuleOrientedLocation() = false

  /**
   * @return `true` if the provided object is a [JavaRamModuleLocation] that refers to the
   * same parent location for the same module, or `false` if it does not.
   */
  override fun equals(other: Any?) = other is JavaRamModuleLocation
      && this.parentLocation == other.parentLocation
      && this.moduleName == other.moduleName

  /**
   * @return the hashcode of this object.
   */
  override fun hashCode() = Objects.hash(this.parentLocation, this.moduleName)

  /**
   * @return a string representation of the location.
   */
  override fun toString() = this.path.toString()
}