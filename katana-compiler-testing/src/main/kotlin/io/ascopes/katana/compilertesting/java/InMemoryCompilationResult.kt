package io.ascopes.katana.compilertesting.java

import java.util.Locale
import javax.annotation.processing.Processor
import javax.tools.Diagnostic.Kind
import javax.tools.Diagnostic.Kind.ERROR
import javax.tools.Diagnostic.Kind.MANDATORY_WARNING
import javax.tools.Diagnostic.Kind.NOTE
import javax.tools.Diagnostic.Kind.OTHER
import javax.tools.Diagnostic.Kind.WARNING
import javax.tools.JavaFileObject
import javax.tools.StandardLocation


/**
 * Results for an in-memory compilation pass.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 * @param outcome the outcome description of the compilation.
 * @param modules the modules passed to the compiler.
 * @param processors the annotation processors passed to the compiler.
 * @param options the options passed to the compiler.
 * @param logs the standard output for the compiler.
 * @param diagnostics the diagnostics that the compiler output, along with call location details.
 * @param fileManager the file manager that was used.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class InMemoryCompilationResult internal constructor(
    val outcome: Outcome,
    val modules: List<String>,
    val processors: List<Processor>,
    val options: List<String>,
    val logs: String,
    val diagnostics: List<InMemoryDiagnostic<out JavaFileObject>>,
    internal val fileManager: InMemoryFileManager
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
    if (!this.predicate()) {
      this.fail("Expected '$description' to succeed, but it failed")
    }
  }

  fun generatedSourceFile(fileName: String) = this.apply {
    this.fileManager
        .getLocationFor(StandardLocation.SOURCE_OUTPUT)
        .getFile(fileName)
        ?: this.fail("Generated source file $fileName did not exist")
  }

  fun generatedSourceFile(fileName: String, moduleName: String) = this.apply {
    this.fileManager
        .getLocationFor(StandardLocation.SOURCE_OUTPUT, moduleName)
        .getFile(fileName)
        ?: this.fail("Generated source file $moduleName/$fileName did not exist")
  }

  fun generatedClassFile(fileName: String) = this.apply {
    this.fileManager
        .getLocationFor(StandardLocation.CLASS_OUTPUT)
        .getFile(fileName)
        ?: this.fail("Generated class file $fileName did not exist")
  }

  fun generatedHeaderFile(fileName: String) = this.apply {
    this.fileManager
        .getLocationFor(StandardLocation.NATIVE_HEADER_OUTPUT)
        .getFile(fileName)
        ?: this.fail("Generated header file $fileName did not exist")
  }

  private fun hadOutcome(expected: Outcome) = this.apply {
    if (this.outcome != expected) {
      this.fail(
          "Unexpected compilation outcome: ${this.outcome.description}",
          expected.description,
          this.outcome.description,
          if (this.outcome is FatalError) this.outcome.reason else null
      )
    }
  }

  private fun hadDiagnosticCount(kind: Kind, expectedCount: Int) = this.apply {
    val actualCount = this.diagnostics.count { it.kind == kind }

    if (expectedCount != actualCount) {
      this.fail(
          "Unexpected number of " + this.pluralizeKind(kind),
          this.quantifyKind(kind, expectedCount),
          this.quantifyKind(kind, actualCount),
      )
    }
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
        ?: this.fail(
            "no $kindName with a message containing '$message' $insensitivity found"
        )
  }

  private fun hadDiagnosticMatching(
      kind: Kind,
      regex: Regex
  ) = this.apply {
    val kindName = this.singularKind(kind)

    this.diagnostics
        .find { it.getMessage(Locale.ROOT).matches(regex) }
        ?: this.fail("no $kindName with a message matching pattern '$regex' found")
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
        1 -> "1 " + this.singularKind(kind)
        else -> "$count " + this.pluralizeKind(kind)
      }

  private fun fail(message: String, expected: Any?, actual: Any?, cause: Throwable? = null) {
    throw InMemoryCompilationAssertionError(message, this, expected, actual, cause)
  }

  private fun fail(message: String, cause: Throwable? = null) {
    throw InMemoryCompilationAssertionError(message, this, cause)
  }

  /**
   * Base marker interface for a compilation outcome.
   *
   * @author Ashley Scopes
   * @since 0.1.0
   */
  sealed interface Outcome {
    val description: String
  }

  /**
   * Marker to indicate a successful compilation.
   *
   * @author Ashley Scopes
   * @since 0.1.0
   */
  object Success : Outcome {
    override val description = "success"
  }

  /**
   * Marker to indicate compilation failed in a non-exceptional way.
   *
   * @author Ashley Scopes
   * @since 0.1.0
   */
  object Failure : Outcome {
    override val description = "failure"
  }

  /**
   * Marker to indicate compilation failed with an exception, unexpectedly.
   *
   * @author Ashley Scopes
   * @since 0.1.0
   */
  class FatalError(
      @Suppress("MemberVisibilityCanBePrivate") val reason: Throwable
  ) : Outcome {
    override val description: String
      get() = "${Companion.description} due to ${this.reason.javaClass.simpleName}"

    companion object {
      const val description: String = "fatal compiler error"
    }
  }
}