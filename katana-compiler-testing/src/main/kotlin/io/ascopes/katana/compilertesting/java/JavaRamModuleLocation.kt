package io.ascopes.katana.compilertesting.java

import java.nio.file.Path
import java.util.Objects
import javax.tools.JavaFileManager.Location

/**
 * Input/output location that occurs in-memory for a module.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 * @param parentLocation the parent location that this location occurs within.
 * @param path the absolute path of the location.
 * @param moduleName the name of the module.
 */
internal data class JavaRamModuleLocation(
    private val parentLocation: Location,
    override val path: Path,
    val moduleName: String
) : JavaRamLocation {
  override fun getName() = this.path.toString()
  override fun isOutputLocation() = this.parentLocation.isOutputLocation
  override fun isModuleOrientedLocation() = false
  override fun equals(other: Any?) = other is JavaRamModuleLocation
      && this.parentLocation == other.parentLocation
      && this.moduleName == other.moduleName

  override fun hashCode() = Objects.hash(this.parentLocation, this.moduleName)
}