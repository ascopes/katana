package io.ascopes.katana.compilertesting.java

import java.util.Locale
import javax.tools.Diagnostic.Kind
import javax.tools.Diagnostic.Kind.ERROR
import javax.tools.Diagnostic.Kind.MANDATORY_WARNING
import javax.tools.Diagnostic.Kind.NOTE
import javax.tools.Diagnostic.Kind.OTHER
import javax.tools.Diagnostic.Kind.WARNING
import javax.tools.JavaFileManager.Location
import javax.tools.JavaFileObject
import javax.tools.StandardLocation.CLASS_OUTPUT
import javax.tools.StandardLocation.NATIVE_HEADER_OUTPUT
import javax.tools.StandardLocation.SOURCE_OUTPUT


/**
 * Results for an in-memory compilation pass.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 * @param outcome the outcome description of the compilation.
 * @param logs the standard output for the compiler.
 * @param diagnostics the diagnostics that the compiler output, along with call location details.
 * @param fileManager the file manager that was used.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class InMemoryCompilationResult(
    val outcome: Outcome,
    val logs: String,
    val diagnostics: List<DiagnosticWithTrace<out JavaFileObject>>,
    private val fileManager: InMemoryFileManager
) {
  /**
   * Assert that the compilation succeeded.
   *
   * @return this object for further call chaining.
   */
  fun succeeded() = this.hadOutcome(Success)

  /**
   * Assert that the compilation succeeded and had no warnings.
   *
   * @return this object for further call chaining.
   */
  fun succeededWithoutWarnings() = this
      .succeeded()
      .hadDiagnosticCount(WARNING, 0)
      .hadDiagnosticCount(MANDATORY_WARNING, 0)

  /**
   * Assert that the compilation failed.
   *
   * @return this object for further call chaining.
   */
  fun failed() = this.hadOutcome(Failure)

  /**
   * Assert that the compilation produced the expected number of errors.
   *
   * @param count the number to expect.
   * @return this object for further call chaining.
   */
  fun hadErrorCount(count: Int) = this.hadDiagnosticCount(ERROR, count)

  /**
   * Assert that the compilation produced an error containing a given message.
   *
   * @param message the message to find.
   * @param ignoreCase `true` to ignore character casing (the default behaviour if unspecified).
   * @return this object for further call chaining.
   */
  fun hadErrorContaining(
      message: String,
      ignoreCase: Boolean = true
  ) = this.hadDiagnosticContaining(
      ERROR,
      message,
      ignoreCase
  )

  /**
   * Assert that the compilation produced an error containing a message that matches the given
   * regular expression.
   *
   * @param regex the regular expression to match.
   * @return this object for further call chaining.
   */
  fun hadErrorMatching(regex: Regex) = this.hadDiagnosticMatching(ERROR, regex)

  /**
   * Assert that the compilation produced an error containing a message that matches the given
   * regular expression.
   *
   * @param regex the regular expression to match.
   * @return this object for further call chaining.
   */
  fun hadErrorMatching(regex: String) = this.hadDiagnosticMatching(ERROR, regex.toRegex())

  /**
   * Assert that the compilation produced the expected number of warnings.
   *
   * This does not include mandatory warnings.
   *
   * @param count the number to expect.
   * @return this object for further call chaining.
   */
  fun hadWarningCount(count: Int) = this.hadDiagnosticCount(WARNING, count)

  /**
   * Assert that the compilation produced a warning containing a given message.
   *
   * This does not include mandatory warnings.
   *
   * @param message the message to find.
   * @param ignoreCase `true` to ignore character casing (the default behaviour if unspecified).
   * @return this object for further call chaining.
   */
  fun hadWarningContaining(
      message: String,
      ignoreCase: Boolean = true
  ) = this.hadDiagnosticContaining(
      WARNING,
      message,
      ignoreCase
  )

  /**
   * Assert that the compilation produced a warning containing a message that matches the given
   * regular expression.
   *
   * This does not include mandatory warnings.
   *
   * @param regex the regular expression to match.
   * @return this object for further call chaining.
   */
  fun hadWarningMatching(regex: Regex) = this.hadDiagnosticMatching(WARNING, regex)

  /**
   * Assert that the compilation produced a warning containing a message that matches the given
   * regular expression.
   *
   * This does not include mandatory warnings.
   *
   * @param regex the regular expression to match.
   * @return this object for further call chaining.
   */
  fun hadWarningMatching(regex: String) = this.hadDiagnosticMatching(WARNING, regex.toRegex())

  /**
   * Assert that the compilation produced the expected number of mandatory warnings.
   *
   * This does not include non-mandatory warnings.
   *
   * @param count the number to expect.
   * @return this object for further call chaining.
   */
  fun hadMandatoryWarningCount(count: Int) = this.hadDiagnosticCount(MANDATORY_WARNING, count)

  /**
   * Assert that the compilation produced a mandatory warning containing a given message.
   *
   * This does not include non-mandatory warnings.
   *
   * @param message the message to find.
   * @param ignoreCase `true` to ignore character casing (the default behaviour if unspecified).
   * @return this object for further call chaining.
   */
  fun hadMandatoryWarningContaining(
      message: String,
      ignoreCase: Boolean = true
  ) = this.hadDiagnosticContaining(
      MANDATORY_WARNING,
      message,
      ignoreCase
  )

  /**
   * Assert that the compilation produced a mandatory warning containing a message that matches the
   * given regular expression.
   *
   * This does not include non-mandatory warnings.
   *
   * @param regex the regular expression to match.
   * @return this object for further call chaining.
   */
  fun hadMandatoryWarningMatching(regex: Regex) = this.hadDiagnosticMatching(
      MANDATORY_WARNING,
      regex
  )


  /**
   * Assert that the compilation produced a mandatory warning containing a message that matches the
   * given regular expression.
   *
   * This does not include non-mandatory warnings.
   *
   * @param regex the regular expression to match.
   * @return this object for further call chaining.
   */
  fun hadMandatoryWarningMatching(regex: String) = this.hadDiagnosticMatching(
      MANDATORY_WARNING,
      regex.toRegex()
  )

  /**
   * Assert that the compilation produced the expected number of notes.
   *
   * @param count the number to expect.
   * @return this object for further call chaining.
   */
  fun hadNoteCount(count: Int) = this.hadDiagnosticCount(NOTE, count)

  /**
   * Assert that the compilation produced a note containing a given message.
   *
   * @param message the message to find.
   * @param ignoreCase `true` to ignore character casing (the default behaviour if unspecified).
   * @return this object for further call chaining.
   */
  fun hadNoteContaining(
      message: String,
      ignoreCase: Boolean = true
  ) = this.hadDiagnosticContaining(
      NOTE,
      message,
      ignoreCase
  )

  /**
   * Assert that the compilation produced a note containing a message that matches the given regular
   * expression.
   *
   * This does not include non-mandatory warnings.
   *
   * @param regex the regular expression to match.
   * @return this object for further call chaining.
   */
  fun hadNoteMatching(regex: Regex) = this.hadDiagnosticMatching(NOTE, regex)

  /**
   * Assert that the compilation produced a note containing a message that matches the given regular
   * expression.
   *
   * This does not include non-mandatory warnings.
   *
   * @param regex the regular expression to match.
   * @return this object for further call chaining.
   */
  fun hadNoteMatching(regex: String) = this.hadDiagnosticMatching(NOTE, regex.toRegex())

  /**
   * Assert that the compilation produced the expected number of 'OTHER'-kinded diagnostics.
   *
   * @param count the number to expect.
   * @return this object for further call chaining.
   */
  fun hadOtherDiagnosticCount(count: Int) = this.hadDiagnosticCount(OTHER, count)

  /**
   * Assert that the compilation produced an 'OTHER'-kinded diagnostic containing a given message.
   *
   * @param message the message to find.
   * @param ignoreCase `true` to ignore character casing (the default behaviour if unspecified).
   * @return this object for further call chaining.
   */
  fun hadOtherDiagnosticContaining(
      message: String,
      ignoreCase: Boolean = true
  ) = this.hadDiagnosticContaining(
      OTHER,
      message,
      ignoreCase
  )


  /**
   * Assert that the compilation produced an 'OTHER'-kinded diagnostic containing a message that
   * matches the given regular expression.
   *
   * This does not include non-mandatory warnings.
   *
   * @param regex the regular expression to match.
   * @return this object for further call chaining.
   */
  fun hadOtherDiagnosticMatching(regex: Regex) = this.hadDiagnosticMatching(OTHER, regex)

  /**
   * Assert that the compilation produced an 'OTHER'-kinded diagnostic containing a message that
   * matches the given regular expression.
   *
   * This does not include non-mandatory warnings.
   *
   * @param regex the regular expression to match.
   * @return this object for further call chaining.
   */
  fun hadOtherDiagnosticMatching(regex: String) = this.hadDiagnosticMatching(OTHER, regex.toRegex())

  /**
   * Assert that the compilation generated a file in the generated class file output location.
   *
   * @param path the path of the generated file to expect.
   * @return this object for further call chaining.
   */
  fun generatedClassFile(path: String) = this.createdFileNamed(CLASS_OUTPUT, path)

  /**
   * Assert that the compilation generated a file in the generated source code output location.
   *
   * @param path the path of the generated file to expect.
   * @return this object for further call chaining.
   */
  fun generatedSourceFile(path: String) = this.createdFileNamed(SOURCE_OUTPUT, path)

  /**
   * Assert that the compilation generated a file in the C/C++ header output location.
   *
   * @param path the path of the generated file to expect.
   * @return this object for further call chaining.
   */
  fun generatedHeaderFile(path: String) = this.createdFileNamed(
      NATIVE_HEADER_OUTPUT,
      path
  )

  /**
   * Check if the given condition is satisfied or not.
   *
   * @param description optional custom description.
   * @param predicate the predicate to match.
   * @return this object for further call chaining.
   */
  fun satisfies(
      description: String? = "custom condition",
      predicate: InMemoryCompilationResult.() -> Boolean
  ) = this.apply {
    this.predicate() || throw JavaCompilerAssertionError(
        "Expected '$description' to succeed, but it failed",
        this.fileManager,
        this.diagnostics,
        this.logs
    )
  }

  private fun hadOutcome(expected: Outcome) = this.apply {
    this.outcome == expected || throw JavaCompilerAssertionError(
        "Unexpected compilation outcome: ${this.outcome.description}",
        this.fileManager,
        this.diagnostics,
        this.logs,
        expected.description,
        this.outcome.description,
        if (this.outcome is FatalError) this.outcome.reason else null
    )
  }

  private fun hadDiagnosticCount(kind: Kind, expectedCount: Int) = this.apply {
    val actualCount = this.diagnostics.count { it.kind == kind }

    expectedCount == actualCount || throw JavaCompilerAssertionError(
        "Unexpected number of " + this.pluralizeKind(kind),
        this.fileManager,
        this.diagnostics,
        this.logs,
        this.quantifyKind(kind, expectedCount),
        this.quantifyKind(kind, actualCount),
    )
  }

  private fun hadDiagnosticContaining(
      kind: Kind,
      message: String,
      ignoreCase: Boolean
  ) = this.apply {
    val kindName = this.singularKind(kind)
    val insensitivity = if (ignoreCase) "(case insensitive)" else "(case sensitive)"

    this.diagnostics
        .find { it.getMessage(Locale.ROOT).contains(message, ignoreCase) }
        ?: throw JavaCompilerAssertionError(
            "no $kindName with a message containing '$message' $insensitivity found",
            this.fileManager,
            this.diagnostics,
            this.logs
        )
  }

  private fun hadDiagnosticMatching(
      kind: Kind,
      regex: Regex
  ) = this.apply {
    val kindName = this.singularKind(kind)

    this.diagnostics
        .find { it.getMessage(Locale.ROOT).matches(regex) }
        ?: throw JavaCompilerAssertionError(
            "no $kindName with a message matching pattern '$regex' found",
            this.fileManager,
            this.diagnostics,
            this.logs
        )
  }

  private fun createdFileNamed(location: Location, path: String) = this.apply {
    this.fileManager.getInMemoryFile(location, path) ?: throw JavaCompilerAssertionError(
        "no ${location.name} file created with path $path",
        this.fileManager,
        this.diagnostics,
        this.logs
    )
  }

  private fun apply(operation: () -> Unit): InMemoryCompilationResult {
    operation()
    return this
  }

  private fun singularKind(kind: Kind) =
      when (kind) {
        ERROR -> "error"
        WARNING -> "warning"
        MANDATORY_WARNING -> "mandatory warning"
        NOTE -> "note"
        OTHER -> "diagnostic message of kind 'OTHER'"
        else -> throw AssertionError("Unknown kind $kind")
      }

  private fun pluralizeKind(kind: Kind) =
      when (kind) {
        ERROR -> "errors"
        WARNING, MANDATORY_WARNING -> "warnings"
        NOTE -> "notes"
        OTHER -> "diagnostic messages of kind 'OTHER'"
        else -> throw AssertionError("Unknown kind $kind")
      }

  private fun quantifyKind(kind: Kind, count: Int) =
      when (count) {
        1 -> this.anify(this.singularKind(kind))
        else -> "$count " + this.pluralizeKind(kind)
      }

  private fun anify(text: String) =
      when (text.first()) {
        'a', 'e', 'i', 'o', 'u', 'y', 'A', 'E', 'I', 'O', 'U', 'Y' -> "an $text"
        else -> "a $text"
      }
}