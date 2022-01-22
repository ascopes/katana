package io.ascopes.katana.compilertesting.java

import javax.tools.JavaFileManager.Location
import javax.tools.StandardLocation

/**
 * Description of the compilation mode for modules.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
enum class JavaCompilationModuleMode(
    private val markerLocation: Location,
    private val disallowedLocation: Location
) {
  /**
   * Legacy non-module or single module source root.
   */
  LEGACY(StandardLocation.SOURCE_PATH, StandardLocation.MODULE_SOURCE_PATH),

  /**
   * Java 9-style multi-module compilation.
   */
  MULTI_MODULE(StandardLocation.MODULE_SOURCE_PATH, StandardLocation.SOURCE_PATH);

  /**
   * If the location is disallowed, throw an exception.
   *
   * @param location the location to check.
   * @throws IllegalStateException if the location is disallowed.
   */
  fun assertLocationAllowed(location: JavaRamLocation) {
    val baseLocation = (location as? JavaRamModuleLocation)
        ?.parentLocation
        ?: location

    if (baseLocation == this.disallowedLocation) {
      throw IllegalStateException(
          """
          Cannot manage files in $location because the compiler module mode is set to ${this.name}.
          
          This usually means you have specified regular sources together with multi-module sources,
          which can lead to the compiler getting confused when deciding how to compile sources.
          
          Make sure you only use single module/non-modular sources, or stick to using multi-module
          sources.
          """.trimIndent()
      )
    }
  }

  companion object {
    /**
     * Get the module mode that the given location implies, or null if the location is not
     * associated with a specific module mode.
     *
     * @param location the location to check.
     * @return the module mode if the location enforces a given module mode by being used, or
     *    null if not applicable.
     */
    fun getModuleModeFor(location: Location): JavaCompilationModuleMode? {
      val baseLocation = (location as? JavaRamModuleLocation)
          ?.parentLocation
          ?: location

      return values().firstOrNull { baseLocation == it.markerLocation }
    }
  }
}