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
internal class JavaDiagnosticListener : DiagnosticListener<JavaFileObject> {
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
    val stackTrace = Thread
        .currentThread()
        .stackTrace
        .drop(Companion.FRAMES_TO_DROP)
        .toList()

    val now = Instant.now()
    this._diagnostics.add(JavaDiagnostic(now, diagnostic, stackTrace))
  }

  private companion object {
    // Last two frames just hold the stacktrace accessor call and this report function,
    // so skip them.
    const val FRAMES_TO_DROP = 2
  }
}