package io.ascopes.katana.compilertesting.java

import java.nio.file.Path
import javax.tools.JavaFileManager.Location

/**
 * Input/output location that occurs in-memory.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 * @param baseLocation the base location to derive this location from.
 * @param path the absolute path of the location.
 */
internal data class InMemoryFileLocation(
    private val baseLocation: Location,
    override val path: Path
) : InMemoryLocation {
  override fun getName() = this.path.toString()
  override fun isOutputLocation() = this.baseLocation.isOutputLocation
  override fun isModuleOrientedLocation() = this.baseLocation.isModuleOrientedLocation
  override fun equals(other: Any?) = this.baseLocation == other
  override fun hashCode() = this.baseLocation.hashCode()
}