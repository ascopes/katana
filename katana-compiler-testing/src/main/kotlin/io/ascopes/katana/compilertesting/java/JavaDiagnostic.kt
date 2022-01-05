package io.ascopes.katana.compilertesting.java

import java.time.Instant
import javax.tools.Diagnostic


/**
 * Diagnostic that also includes the stacktrace of the callee.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 * @param timestamp the timestamp that the diagnostic was reported at.
 * @param diagnostic the original diagnostic to wrap.
 * @param stacktrace the stacktrace of the call to log the diagnostic.
 */
class JavaDiagnostic<S> internal constructor(
    val timestamp: Instant,
    private val diagnostic: Diagnostic<S>,
    val stacktrace: List<StackTraceElement>
) : Diagnostic<S> by diagnostic {

  override fun equals(other: Any?) = when (other) {
    is JavaDiagnostic<*> -> this.diagnostic == other.diagnostic
    is Diagnostic<*> -> this.diagnostic == other
    else -> false
  }

  override fun hashCode() = this.diagnostic.hashCode()

  override fun toString() = this.diagnostic.toString()
}