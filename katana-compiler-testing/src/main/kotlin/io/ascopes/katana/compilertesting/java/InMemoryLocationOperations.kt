package io.ascopes.katana.compilertesting.java

import java.nio.file.Path
import javax.tools.FileObject
import javax.tools.JavaFileManager.Location

/**
 * Operations that can be performed on a wrapped location.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
interface InMemoryLocationOperations {
  /**
   * The location being operated on.
   */
  val location: Location

  /**
   * The name of the module that the location concerns, or `null` if not operating on a named
   * module in a multimodule compilation.
   */
  val moduleName: String?

  /**
   * The base path for the location.
   */
  val path: Path

  /**
   * Create a file and fill it with the given byte content.
   *
   * @param fileName the file name.
   * @param content the file content.
   * @return the path to the created file.
   */
  fun createFile(fileName: String, content: ByteArray): Path

  /**
   * Create a file and fill it with the given lines of text.
   *
   * @param fileName the file name.
   * @param lines the lines of text.
   * @return the path to the created file.
   */
  fun createFile(fileName: String, vararg lines: String): Path

  /**
   * Get an existing file.
   *
   * @param fileName the name of the file.
   * @return the file object, if it exists, else null.
   */
  fun getFile(fileName: String): FileObject?
}