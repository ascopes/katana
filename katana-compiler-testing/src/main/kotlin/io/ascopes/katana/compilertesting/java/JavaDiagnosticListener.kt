package io.ascopes.katana.compilertesting.java

import java.time.Instant
import javax.tools.Diagnostic
import javax.tools.DiagnosticListener
import javax.tools.JavaFileObject

/**
 * Collector of diagnostics.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
internal class JavaDiagnosticListener(
    // Visible for testing purposes only.
    internal val stackTraceProvider: () -> List<StackTraceElement>
) : DiagnosticListener<JavaFileObject> {
  private val _diagnostics = mutableListOf<JavaDiagnostic<out JavaFileObject>>()

  /**
   * The collection of collected diagnostics.
   */
  val diagnostics: List<JavaDiagnostic<out JavaFileObject>>
    get() = this._diagnostics

  /**
   * Report a diagnostic.
   *
   * @param diagnostic the diagnostic to report.
   */
  override fun report(diagnostic: Diagnostic<out JavaFileObject>) {
    val now = Instant.now()
    val stackTrace = this.stackTraceProvider()
    this._diagnostics.add(JavaDiagnostic(now, diagnostic, stackTrace))
  }
}