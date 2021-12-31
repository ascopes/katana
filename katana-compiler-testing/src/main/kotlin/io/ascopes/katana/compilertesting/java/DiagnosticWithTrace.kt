package io.ascopes.katana.compilertesting.java

import java.util.Locale
import javax.tools.Diagnostic


/**
 * Diagnostic that also includes the stacktrace of the callee.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 * @param diagnostic the original diagnostic to wrap.
 * @param stacktrace the stacktrace of the call to log the diagnostic.
 */
class DiagnosticWithTrace<S>(
    private val diagnostic: Diagnostic<S>,
    private val stacktrace: List<StackTraceElement>
) : Diagnostic<S> by diagnostic {
  
  override fun equals(other: Any?): Boolean {
    if (other is DiagnosticWithTrace<*>) {
      return this.diagnostic = other.diagnostic
    }

    if (other is Diagnostic<*>) {
      return this.diagnostic == other
    }

    return false
  }
  
  override fun hashCode() = this.diagnostic.hashCode()

  override fun toString(): String {
    return StringBuilder()
        .append(this.kind.toString())
        .append(" - ")
        .append(this.code)
        .append('\n')
        .append(this.getMessage(Locale.ROOT))
        .append("\n\nDiagnostic was reported at:\n")

        .also { builder ->
          this.stacktrace.forEach { frame ->
            builder
                .append('\t')
                .append(frame)
                .append('\n')
          }
        }
        .toString()
  }
}
