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
 * @param moduleName the name of the module, or null if not applicable.
 */
class InMemoryLocation(
    private val baseLocation: Location,
    val path: Path,
    val moduleName: String?
) : Location {
  override fun getName() = this.path.toString()
  override fun isOutputLocation() = this.baseLocation.isOutputLocation
  override fun isModuleOrientedLocation() = this.moduleName != null
      && this.baseLocation.isModuleOrientedLocation
}