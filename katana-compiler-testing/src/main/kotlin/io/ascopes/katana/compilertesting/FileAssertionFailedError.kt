package io.ascopes.katana.compilertesting

import java.nio.file.Path
import org.opentest4j.AssertionFailedError

/**
 * Exception thrown when an assertion on a file fails.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
class FileAssertionFailedError : AssertionFailedError {
  private val fileName: Path

  /**
   * @param message the message to add to the exception.
   * @param fileName the path of the file that is the target of the exception.
   * @param expected the expected result.
   * @param actual the actual result.
   */
  constructor(
      message: String,
      fileName: Path,
      expected: Any?,
      actual: Any?
  ) : super(message, expected, actual) {
    this.fileName = fileName
  }

  /**
   * @param message the message to add to the exception.
   * @param fileName the path of the file that is the target of the exception.
   */
  constructor(message: String, fileName: Path) : super(message) {
    this.fileName = fileName
  }

  override val message: String
    get() {
      val baseMessage = super.message
      return "In $fileName: $baseMessage"
    }
}