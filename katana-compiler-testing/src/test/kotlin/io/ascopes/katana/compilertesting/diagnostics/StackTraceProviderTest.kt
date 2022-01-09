package io.ascopes.katana.compilertesting.diagnostics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StackTraceProviderTest {
  @Test
  fun `the correct stack trace is provided by threadStackTraceProvider`() {
    val expectedStackTrace = Thread
        .currentThread()
        .stackTrace
        // Drop the call to Thread#getStackTrace()
        .drop(1)
        .takeWhile { !it.className.startsWith("org.junit.platform.") }
        .joinToString(separator = "\n", transform = this::frameToString)

    val actualStackTrace = StackTraceProvider
        .threadStackTraceProvider
        .invoke()
        .joinToString(separator = "\n", transform = this::frameToString)

    assertEquals(expectedStackTrace, actualStackTrace)
  }

  private fun frameToString(frame: StackTraceElement) =
      frame.className.toString() + ":" + frame.methodName + " from " + frame.fileName
}