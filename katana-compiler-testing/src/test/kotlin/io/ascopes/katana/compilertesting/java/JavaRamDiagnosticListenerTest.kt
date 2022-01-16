package io.ascopes.katana.compilertesting.java

import io.ascopes.katana.compilertesting.StackTraceProvider
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import javax.tools.Diagnostic
import javax.tools.JavaFileObject

class JavaRamDiagnosticListenerTest {
  private val dummyStackTraceProvider = object : StackTraceProvider {
    val frames = listOf(
        StackTraceElement("foo.bar.Baz", "somethingThatReportsStuff", "Baz.kt", 12),
        StackTraceElement("foo.bar.Bork", "bleepBloop", "Bork.kt", 117),
        StackTraceElement("foo.bar.Qux", "meow", "Qux.kt", 34)
    )

    override fun invoke() = frames
  }

  @Test
  fun `reporting a diagnostic will wrap it in a JavaDiagnostic object`() {
    val listener = JavaDiagnosticListener(this.dummyStackTraceProvider)
    val diagnostic = mockk<Diagnostic<JavaFileObject>>()

    listener.report(diagnostic)

    assertEquals(1, listener.diagnostics.size)
    val wrappedDiagnostic = listener.diagnostics.first()

    assertEquals(diagnostic, wrappedDiagnostic.diagnostic)
  }

  @Test
  fun `reporting a diagnostic will record the time it was reported at`() {
    val listener = JavaDiagnosticListener(this.dummyStackTraceProvider)
    val diagnostic = mockk<Diagnostic<JavaFileObject>>()

    val before = Instant.now()
    listener.report(diagnostic)
    val after = Instant.now()

    assumeTrue(
        after.isAfter(before),
        "System time has changed mid-test, so correct timestamps cannot be checked."
    )

    assertEquals(1, listener.diagnostics.size)
    val wrappedDiagnostic = listener.diagnostics.first()

    assertFalse(
        before.isAfter(wrappedDiagnostic.timestamp),
        "'before' is after diagnostic reporting time"
    )

    assertFalse(
        after.isBefore(wrappedDiagnostic.timestamp),
        "'after' is before diagnostic reporting time"
    )
  }

  @Test
  fun `reporting a diagnostic will take the current stack trace`() {
    val listener = JavaDiagnosticListener(this.dummyStackTraceProvider)
    val diagnostic = mockk<Diagnostic<JavaFileObject>>()

    listener.report(diagnostic)

    assertEquals(1, listener.diagnostics.size)
    val wrappedDiagnostic = listener.diagnostics.first()

    assertInstanceOf(JavaRamDiagnostic::class.java, wrappedDiagnostic)
    assertEquals(this.dummyStackTraceProvider.frames, wrappedDiagnostic.stacktrace)
  }
}