package io.ascopes.katana.compilertesting.core

import io.ascopes.katana.compilertesting.core.ExceptionAssertionsTest.ExceptionWithCause
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ExceptionAssertionFailedErrorTest {
  @Test
  fun `printStackTrace(PrintStream) includes the exception being tested`() {
    // Given
    val cause = RuntimeException("something went wrong at runtime!")
        .apply { fillInStackTrace() }
    val ex = ExceptionWithCause("something or other went wrong", cause)
        .apply { fillInStackTrace() }

    val stackTrace = Thread.currentThread().stackTrace
    val assertionEx = ExceptionAssertionFailedError("Your test failed", ex)
        .apply { setStackTrace(stackTrace) }

    val expectedStackTrace = StringBuilder()
        .append(ExceptionAssertionFailedError::class.java.name)
        .append(": ")
        .appendLine("Your test failed")
        .apply {
          stackTrace.forEach {
            append("\tat ").appendLine(it.toString())
          }
        }
        .append("Exception being tested was: ")
        .append(ex.stackTraceToString())
        .toString()

    // When
    val stream = ByteArrayOutputStream()
    assertionEx.printStackTrace(PrintStream(stream))

    // Then
    Assertions.assertEquals(
        expectedStackTrace,
        stream.toByteArray().toString(StandardCharsets.UTF_8)
    )
  }

  @Test
  fun `printStackTrace(PrintWriter) includes the exception being tested`() {
    // Given
    val cause = RuntimeException("something went wrong at runtime!")
        .apply { fillInStackTrace() }
    val ex = ExceptionWithCause("something or other went wrong", cause)
        .apply { fillInStackTrace() }

    val stackTrace = Thread.currentThread().stackTrace
    val assertionEx = ExceptionAssertionFailedError("Your test was bad", ex)
        .apply { setStackTrace(stackTrace) }

    val expectedStackTrace = StringBuilder()
        .append(ExceptionAssertionFailedError::class.java.canonicalName)
        .append(": ")
        .appendLine("Your test was bad")
        .apply {
          stackTrace.forEach {
            append("\tat ").appendLine(it.toString())
          }
        }
        .append("Exception being tested was: ")
        .append(ex.stackTraceToString())
        .toString()

    // When
    val writer = StringWriter()
    assertionEx.printStackTrace(PrintWriter(writer))

    // Then
    Assertions.assertEquals(expectedStackTrace, writer.toString())
  }
}