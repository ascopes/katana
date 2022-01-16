package io.ascopes.katana.compilertesting.java

import java.time.Instant

/**
 * Collector of diagnostics.
 *
 * @param stackTraceProvider the provider of caller stack traces to use.
 * @author Ashley Scopes
 * @since 0.1.0
 */
internal class JavaDiagnosticListener(
    // Visible for testing purposes only.
    internal val stackTraceProvider: () -> List<StackTraceElement>
) : DiagnosticListenerImpl {
  private val diagnosticsList = mutableListOf<JavaRamDiagnosticImpl>()

  /**
   * The collection of collected diagnostics.
   */
  val diagnostics: List<JavaRamDiagnosticImpl>
    // Return a shallow copy.
    get() = ArrayList(this.diagnosticsList)

  /**
   * Report a diagnostic.
   *
   * @param diagnostic the diagnostic to report.
   */
  override fun report(diagnostic: DiagnosticImpl) {
    val now = Instant.now()
    val stackTrace = this.stackTraceProvider()
    this.diagnosticsList.add(JavaRamDiagnostic(now, diagnostic, stackTrace))
  }
}