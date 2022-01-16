package io.ascopes.katana.compilertesting

import java.io.Flushable
import java.io.PrintStream
import java.io.PrintWriter
import org.opentest4j.AssertionFailedError

/**
 * Assertion error type thrown if an exception assertion does not succeed.
 *
 * This contains logic to document the exception being tested in the stacktrace.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
class ExceptionAssertionFailedError : AssertionFailedError {
  val exceptionThatWasTested: Throwable

  internal constructor(
      message: String,
      expected: Any?,
      actual: Any?,
      exceptionBeingTested: Throwable
  ) : super(message, expected, actual) {
    this.exceptionThatWasTested = exceptionBeingTested
  }

  internal constructor(
      message: String,
      exceptionBeingTested: Throwable
  ) : super(message) {
    this.exceptionThatWasTested = exceptionBeingTested
  }

  override fun printStackTrace(stream: PrintStream) {
    super.printStackTrace(stream)
    this.appendExceptionToTestToStackTrace(stream)
  }

  override fun printStackTrace(writer: PrintWriter) {
    super.printStackTrace(writer)
    this.appendExceptionToTestToStackTrace(writer)
  }

  private fun <T> appendExceptionToTestToStackTrace(appendable: T)
      where T : Appendable, T : Flushable {

    appendable.append("Exception being tested was: ")

    val stackTrace = this.exceptionThatWasTested.stackTraceToString()

    appendable.append(stackTrace)
    appendable.flush()
  }
}