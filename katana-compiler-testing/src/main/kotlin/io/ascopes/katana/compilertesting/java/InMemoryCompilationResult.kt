package io.ascopes.katana.compilertesting.java

import javax.tools.JavaFileObject


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
  fun assertSucceeded() = this.apply { this.assertOutcomeIs<Ok>() }

  /**
   * Assert that the compilation failed un-exceptionally.
   *
   * @return this object for further call chaining.
   */
  fun assertFailed() = this.apply { this.assertOutcomeIs<Failure>() }

  private inline fun <reified T : Outcome> assertOutcomeIs() {
    if (this.outcome !is T) {
      val thisDescription = if (this.outcome is FatalError) {
        "Fatal Compiler Error (${this.outcome.reason.javaClass.simpleName})"
      } else {
        this.outcome::class.java.simpleName
      }

      this.assertionError(
          T::class.java.simpleName,
          thisDescription,
          "Unexpected compilation result"
      )
    }
  }

  private fun assertionError(expected: Any, actual: Any, message: String): Nothing {
    val messageBuilder = StringBuilder(message)
        .append("\n")
        .append("\tExpected: <$expected>\n")
        .append("\tActual:   <$actual>")

    throw AssertionError(messageBuilder.toString())
  }

  private fun apply(operation: () -> Unit): InMemoryCompilationResult {
    operation()
    return this
  }
}