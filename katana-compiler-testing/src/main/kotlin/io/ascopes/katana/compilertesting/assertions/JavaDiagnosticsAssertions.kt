package io.ascopes.katana.compilertesting.assertions

import io.ascopes.katana.compilertesting.diagnostics.JavaDiagnostic
import javax.tools.Diagnostic
import org.opentest4j.AssertionFailedError

/**
 * Assertions to apply to all reported diagnostics as a group.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class JavaDiagnosticsAssertions internal constructor(
    target: List<JavaDiagnostic<*>>
) : CommonAssertions<List<JavaDiagnostic<*>>, JavaDiagnosticsAssertions>(target) {

  /**
   * Assert that no errors were reported.
   *
   * @return this assertion object for further checks.
   */
  fun hasNoErrors() = apply {
    this.hasMatchingDiagnosticCount("Expected no errors to be reported", 0) {
      it.kind == Diagnostic.Kind.ERROR
    }
  }

  /**
   * Assert that no warnings or mandatory warnings were reported.
   *
   * @return this assertion object for further checks.
   */
  fun hasNoWarnings() = apply {
    this.hasMatchingDiagnosticCount("Expected no warnings to be reported", 0) {
      it.kind == Diagnostic.Kind.WARNING || it.kind == Diagnostic.Kind.MANDATORY_WARNING
    }
  }

  /**
   * Assert that no notes were reported.
   *
   * @return this assertion object for further checks.
   */
  fun hasNoNotes() = apply {
    this.hasMatchingDiagnosticCount("Expected no notes to be reported", 0) {
      it.kind == Diagnostic.Kind.NOTE
    }
  }

  /**
   * Assert that no 'other' diagnostics were reported.
   *
   * @return this assertion object for further checks.
   */
  fun hasNoOtherDiagnostics() = apply {
    this.hasMatchingDiagnosticCount("Expected no OTHER-kinded diagnostics to be reported", 0) {
      it.kind == Diagnostic.Kind.OTHER
    }
  }

  /**
   * Assert that the given number of errors were reported.
   *
   * @param count the number of diagnostics to expect.
   * @return this assertion object for further checks.
   */
  fun hasErrorCount(count: Int) = apply {
    this.hasMatchingDiagnosticCount("Expected $count error(s) to be reported", count) {
      it.kind == Diagnostic.Kind.ERROR
    }
  }

  /**
   * Assert that the given number of warnings and mandatory warnings were reported.
   *
   * @param count the number of diagnostics to expect.
   * @return this assertion object for further checks.
   */
  fun hasWarningCount(count: Int) = apply {
    this.hasMatchingDiagnosticCount("Expected $count warnings(s) to be reported", count) {
      it.kind == Diagnostic.Kind.WARNING || it.kind == Diagnostic.Kind.MANDATORY_WARNING
    }
  }

  /**
   * Assert that the given number of notes were reported.
   *
   * @param count the number of diagnostics to expect.
   * @return this assertion object for further checks.
   */
  fun hasNoteCount(count: Int) = apply {
    this.hasMatchingDiagnosticCount("Expected $count note(s) to be reported", count) {
      it.kind == Diagnostic.Kind.NOTE
    }
  }

  /**
   * Assert that the given number of 'other' diagnostics were reported.
   *
   * @param count the number of diagnostics to expect.
   * @return this assertion object for further checks.
   */
  fun hasOtherDiagnosticsCount(count: Int) = apply {
    this.hasMatchingDiagnosticCount(
        "Expected $count OTHER-kinded diagnostics(s) to be reported",
        count
    ) {
      it.kind == Diagnostic.Kind.OTHER
    }
  }

  /**
   * Assert that the given number of diagnostics matching the given predicate were found.
   *
   * @param message the informative message to show on failure.
   * @param count the number of diagnostics to expect.
   * @param predicate the predicate to use.
   * @return this assertion object for further checks.
   */
  fun hasMatchingDiagnosticCount(
      message: String,
      count: Int,
      predicate: (JavaDiagnostic<*>) -> Boolean
  ) = apply {
    val matches = target
        .filter(predicate)
        .toList()

    if (matches.size != count) {
      failUnexpectedDiagnosticCount(message, count, matches.size)
    }
  }

  private fun failUnexpectedDiagnosticCount(
      message: String,
      expectedCount: Int,
      actualCount: Int
  ): Nothing {
    fun pluralise(count: Int): String {
      return when (count) {
        0 -> "no matches"
        1 -> "1 match"
        else -> "$count matches"
      }
    }

    throw AssertionFailedError(
        message,
        pluralise(expectedCount),
        pluralise(actualCount)
    )
  }
}