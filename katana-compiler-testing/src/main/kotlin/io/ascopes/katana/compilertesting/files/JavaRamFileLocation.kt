package io.ascopes.katana.compilertesting.files

import java.nio.file.Path
import javax.tools.JavaFileManager.Location

/**
 * Input/output location that occurs in-memory.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 * @param baseLocation the base location to derive this location from.
 * @param path the absolute path of the location.
 */
data class JavaRamFileLocation internal constructor(
    private val baseLocation: Location,
    override val path: Path
) : JavaRamLocation {
  override fun getName() = this.path.toString()

  override fun isOutputLocation() = this.baseLocation.isOutputLocation

  override fun isModuleOrientedLocation() = this.baseLocation.isModuleOrientedLocation

  override fun equals(other: Any?): Boolean {
    return when (other) {
      is JavaRamFileLocation -> this.baseLocation == other.baseLocation
      is Location -> this.baseLocation == other
      else -> false
    }
  }

  override fun hashCode() = this.baseLocation.hashCode()

  override fun toString() = this.path.toString()
}