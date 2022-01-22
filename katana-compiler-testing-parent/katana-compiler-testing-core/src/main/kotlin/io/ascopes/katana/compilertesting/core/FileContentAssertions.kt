package io.ascopes.katana.compilertesting.core

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.file.Path
import java.util.regex.Pattern
import org.opentest4j.IncompleteExecutionException


/**
 * Assertions on the contents of binary files that may optionally be decoded to text files
 * ad-hoc.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
@Suppress("MemberVisibilityCanBePrivate")
class FileContentAssertions
constructor(
    private val fileName: Path,
    private val fileContent: ByteArray,
    private val charset: Charset
) : CommonAssertions<ByteArray, FileContentAssertions>(fileContent) {

  private val fileText: String by lazy {
    charset
        .newDecoder()
        .onUnmappableCharacter(CodingErrorAction.REPORT)
        .onMalformedInput(CodingErrorAction.REPORT)
        .decode(ByteBuffer.wrap(fileContent))
        .toString()
  }

  /**
   * Assert that the file has no content in it.
   *
   * @return this assertion object for further call chaining.
   */
  fun hasNoContent() = apply {
    if (this.fileContent.isNotEmpty()) {
      throw FileAssertionFailedError(
          "File is not empty",
          fileName,
          "",
          fileContent
      )
    }
  }

  /**
   * Assert that the file content matches a given byte array.
   *
   * @param expected the expected byte array.
   * @return this object for further call chaining.
   */
  fun hasContent(expected: ByteArray) = apply {
    if (!fileContent.contentEquals(expected)) {
      throw FileAssertionFailedError(
          "File byte content does not match the expected content",
          fileName,
          expected,
          fileContent
      )
    }
  }

  /**
   * Assert that the file content matches a given string.
   *
   * @param expected the expected string.
   * @param ignoreCase `true` to ignore case sensitivity, or `false` (default) otherwise.
   * @return this object for further call chaining.
   */
  @JvmOverloads
  fun hasContent(expected: String, ignoreCase: Boolean = false) = apply {
    if (!fileText.contentEquals(expected, ignoreCase)) {
      val sensitivity = if (ignoreCase) "case-insensitive" else "case-sensitive"

      throw FileAssertionFailedError(
          "File string content does not match the expected result ($sensitivity)",
          fileName,
          expected,
          fileText
      )
    }
  }

  /**
   *  Assert that the file has content matching the given pattern.
   *
   * @param pattern the pattern to search for.
   * @return this assertion object for further checks.
   */
  fun hasContentMatching(pattern: String) = hasContentMatching(pattern.toRegex())

  /**
   *  Assert that the file has content matching the given pattern.
   *
   * @param pattern the pattern to search for.
   * @param flags the [Pattern] flags to allow.
   * @return this assertion object for further checks.
   */
  fun hasContentMatching(pattern: String, vararg flags: Int) = apply {
    val combinedFlags = flags.fold(0) { a, b -> a or b }
    hasContentMatching(pattern.toPattern(combinedFlags))
  }

  /**
   * Assert that the file has content matching the given pattern.
   *
   * @param pattern the pattern to search for.
   * @param flags the [RegexOption] flags to allow.
   * @return this assertion object for further checks.
   */
  fun hasContentMatching(pattern: String, vararg flags: RegexOption) =
      hasContentMatching(Regex(pattern, flags.toSet()))

  /**
   * Assert that the file has content matching the given pattern.
   *
   * @param pattern the pattern to search for.
   * @return this assertion object for further checks.
   */
  fun hasContentMatching(pattern: Pattern) = hasContentMatching(pattern.toRegex())

  /**
   * Assert that the file has content matching the given pattern.
   *
   * @param pattern the pattern to search for.
   * @return this assertion object for further checks.
   */
  fun hasContentMatching(pattern: Regex) = apply {
    if (pattern.matchEntire(fileText) == null) {
      val patternStr = pattern.pattern.replace("/", "\\/")
      val patternOpts = pattern.options.map { it.name }.sorted().joinToString()
          .ifEmpty { "none" }

      val message = StringBuilder()
          .append(" did not match pattern /")
          .append(patternStr)
          .appendLine("/")
          .append("Options: <")
          .append(patternOpts)
          .appendLine(">")
          .appendLine("Content:")
          .appendLine(fileText.prependIndent(">>> "))

      throw FileAssertionFailedError(message.toString(), fileName)
    }
  }

  /**
   * Check that the text content of the file satisfies a set of expectations.
   *
   * Same as [satisfies], except it operates on the [String] content rather than the
   * [ByteArray] content.
   *
   * @param expectations the expectations to perform.
   * @return this object for further call chaining.
   */
  fun textSatisfies(expectations: Expectations<String>) = this.apply {
    try {
      expectations(fileText)
    } catch (ex: AssertionError) {
      // Rethrow.
      throw ex
    } catch (ex: Throwable) {
      throw IncompleteExecutionException("Unexpected exception thrown", ex)
    }
  }
}