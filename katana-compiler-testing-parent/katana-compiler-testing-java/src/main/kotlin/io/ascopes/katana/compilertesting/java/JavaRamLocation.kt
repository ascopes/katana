package io.ascopes.katana.compilertesting.java

import java.nio.file.Path
import javax.tools.JavaFileManager.Location


/**
 * Base for any in-memory location.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
interface JavaRamLocation : Location {
  /**
   * The path to the location.
   */
  val path: Path
}