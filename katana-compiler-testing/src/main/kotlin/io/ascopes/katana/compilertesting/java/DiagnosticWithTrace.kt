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
    private val stacktrace: Array<StackTraceElement>
) : Diagnostic<S> by diagnostic {

  override fun toString(): String {
    return StringBuilder()
        .append(this.kind.toString())
        .append(" - ")
        .append(this.code)
        .append('\n')
        .append(this.getMessage(Locale.ROOT))
        .append('\n')
        .also { builder ->
          this.stacktrace.forEach { frame ->
            builder
                .append("\tat ")
                .append(frame)
                .append('\n')
          }
        }
        .toString()
  }
}