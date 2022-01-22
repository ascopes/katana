package io.ascopes.katana.compilertesting.java

import java.time.Instant
import javax.tools.Diagnostic
import javax.tools.DiagnosticListener
import javax.tools.JavaFileObject
import mu.KotlinLogging

/**
 * Collector of diagnostics.
 *
 * @param stackTraceProvider the provider of caller stack traces to use.
 * @author Ashley Scopes
 * @since 0.1.0
 */
class JavaDiagnosticListener(
    // Visible for testing purposes only.
    internal val stackTraceProvider: () -> List<StackTraceElement>
) : DiagnosticListener<JavaFileObject> {
  private val logger = KotlinLogging.logger { }

  private val diagnosticsList = mutableListOf<JavaRamDiagnostic<out JavaFileObject>>()

  /**
   * The collection of collected diagnostics.
   */
  val diagnostics: List<JavaRamDiagnostic<out JavaFileObject>>
    // Return a shallow copy.
    get() = ArrayList(this.diagnosticsList)

  /**
   * Report a diagnostic.
   *
   * @param diagnostic the diagnostic to report.
   */
  override fun report(diagnostic: Diagnostic<out JavaFileObject>) {
    logger.info { diagnostic }

    val now = Instant.now()

    val stackTrace = this.stackTraceProvider()
    this.diagnosticsList.add(JavaRamDiagnostic(now, diagnostic, stackTrace))
  }
}