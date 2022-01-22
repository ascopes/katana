package io.ascopes.katana.compilertesting.java

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import java.util.Locale
import java.util.UUID
import javax.tools.Diagnostic
import javax.tools.Diagnostic.Kind
import javax.tools.JavaFileObject
import kotlin.random.Random
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class JavaRamDiagnosticTest {
  @Nested
  inner class EqualsTest {
    @Test
    fun `is equal to itself`() {
      val diagnostic = JavaRamDiagnostic(
          mockk("timestamp"),
          mockk<Diagnostic<*>>("diagnostic"),
          mockk("stacktrace")
      )

      assertEquals(diagnostic, diagnostic)
    }

    @Test
    fun `is equal to another JavaDiagnostic instance holding the same inner diagnostic`() {
      val innerDiagnostic = mockk<Diagnostic<*>>()

      val diagnostic1 = JavaRamDiagnostic(
          mockk("timestamp1"),
          innerDiagnostic,
          mockk("stacktrace1")
      )

      val diagnostic2 = JavaRamDiagnostic(
          mockk("timestamp2"),
          innerDiagnostic,
          mockk("stacktrace2")
      )

      assertEquals(diagnostic1, diagnostic2)
    }

    @Test
    fun `is equal to the inner diagnostic`() {
      val innerDiagnostic = mockk<Diagnostic<*>>()
      val diagnostic = JavaRamDiagnostic(
          mockk("timestamp"),
          innerDiagnostic,
          mockk("stacktrace")
      )

      assertEquals(diagnostic, innerDiagnostic)
    }

    @Test
    fun `is not equal to a different diagnostic`() {
      val diagnostic1 = JavaRamDiagnostic(
          mockk("timestamp1"),
          mockk<Diagnostic<*>>("innerDiagnostic1"),
          mockk("stacktrace1")
      )
      val diagnostic2 = JavaRamDiagnostic(
          mockk("timestamp2"),
          mockk<Diagnostic<*>>("innerDiagnostic2"),
          mockk("stacktrace2")
      )

      assertNotEquals(diagnostic1, diagnostic2)
    }
  }

  @Test
  fun `hashCode() matches the inner diagnostic hashCode()`() {
    val innerDiagnostic = mockk<Diagnostic<*>>()
    val randomHashCode = Random.nextInt()

    every { innerDiagnostic.hashCode() } returns randomHashCode

    val diagnostic = JavaRamDiagnostic(
        mockk("timestamp"),
        innerDiagnostic,
        mockk("stacktrace")
    )

    assertEquals(diagnostic.hashCode(), randomHashCode)
  }

  @Test
  fun `getKind() delegates to the inner diagnostic`() {
    val innerDiagnostic = mockk<Diagnostic<JavaFileObject>>()
    val stackFrames = mockk<List<StackTraceElement>>()
    val timestamp = mockk<Instant>()
    val kind = mockk<Kind>()

    every { innerDiagnostic.kind } returns kind

    val diagnostic = JavaRamDiagnostic(timestamp, innerDiagnostic, stackFrames)

    assertEquals(diagnostic.kind, kind)
  }

  @Test
  fun `getSource() delegates to the inner diagnostic`() {
    val innerDiagnostic = mockk<Diagnostic<*>>()
    val stackFrames = mockk<List<StackTraceElement>>()
    val timestamp = mockk<Instant>()
    val source = Any()

    every { innerDiagnostic.source } returns source

    val diagnostic = JavaRamDiagnostic(timestamp, innerDiagnostic, stackFrames)

    assertEquals(diagnostic.source, source)
  }

  @Test
  fun `getPosition() delegates to the inner diagnostic`() {
    val innerDiagnostic = mockk<Diagnostic<*>>()
    val stackFrames = mockk<List<StackTraceElement>>()
    val timestamp = mockk<Instant>()
    val position = Random.nextLong(0, Long.MAX_VALUE)

    every { innerDiagnostic.position } returns position

    val diagnostic = JavaRamDiagnostic(timestamp, innerDiagnostic, stackFrames)

    assertEquals(diagnostic.position, position)
  }

  @Test
  fun `getStartPosition() delegates to the inner diagnostic`() {
    val innerDiagnostic = mockk<Diagnostic<*>>()
    val stackFrames = mockk<List<StackTraceElement>>()
    val timestamp = mockk<Instant>()
    val position = Random.nextLong(0, Long.MAX_VALUE)

    every { innerDiagnostic.startPosition } returns position

    val diagnostic = JavaRamDiagnostic(timestamp, innerDiagnostic, stackFrames)

    assertEquals(diagnostic.startPosition, position)
  }

  @Test
  fun `getEndPosition() delegates to the inner diagnostic`() {
    val innerDiagnostic = mockk<Diagnostic<*>>()
    val stackFrames = mockk<List<StackTraceElement>>()
    val timestamp = mockk<Instant>()
    val position = Random.nextLong(0, Long.MAX_VALUE)

    every { innerDiagnostic.endPosition } returns position

    val diagnostic = JavaRamDiagnostic(timestamp, innerDiagnostic, stackFrames)

    assertEquals(diagnostic.endPosition, position)
  }

  @Test
  fun `getLineNumber() delegates to the inner diagnostic`() {
    val innerDiagnostic = mockk<Diagnostic<*>>()
    val stackFrames = mockk<List<StackTraceElement>>()
    val timestamp = mockk<Instant>()
    val lineNumber = Random.nextLong(0, Long.MAX_VALUE)

    every { innerDiagnostic.lineNumber } returns lineNumber

    val diagnostic = JavaRamDiagnostic(timestamp, innerDiagnostic, stackFrames)

    assertEquals(diagnostic.lineNumber, lineNumber)
  }

  @Test
  fun `getColumnNumber() delegates to the inner diagnostic`() {
    val innerDiagnostic = mockk<Diagnostic<*>>()
    val stackFrames = mockk<List<StackTraceElement>>()
    val timestamp = mockk<Instant>()
    val columnNumber = Random.nextLong(0, Long.MAX_VALUE)

    every { innerDiagnostic.columnNumber } returns columnNumber

    val diagnostic = JavaRamDiagnostic(timestamp, innerDiagnostic, stackFrames)

    assertEquals(diagnostic.columnNumber, columnNumber)
  }

  @Test
  fun `getCode() delegates to the inner diagnostic`() {
    val innerDiagnostic = mockk<Diagnostic<*>>()
    val stackFrames = mockk<List<StackTraceElement>>()
    val timestamp = mockk<Instant>()
    val code = UUID.randomUUID().toString()

    every { innerDiagnostic.code } returns code

    val diagnostic = JavaRamDiagnostic(timestamp, innerDiagnostic, stackFrames)

    assertEquals(diagnostic.code, code)
  }

  @Test
  fun `getMessage(Locale) delegates to the inner diagnostic`() {
    val innerDiagnostic = mockk<Diagnostic<*>>()
    val stackFrames = mockk<List<StackTraceElement>>()
    val timestamp = mockk<Instant>()
    val message = UUID.randomUUID().toString()

    every { innerDiagnostic.getMessage(any()) } returns message

    val diagnostic = JavaRamDiagnostic(timestamp, innerDiagnostic, stackFrames)

    assertEquals(diagnostic.getMessage(Locale.CHINESE), message)

    verify { innerDiagnostic.getMessage(Locale.CHINESE) }
  }

  @Test
  fun `toString() returns the inner diagnostic string representation`() {
    val innerDiagnosticName = "<dummy diagnostic name " + UUID.randomUUID() + ">"
    val innerDiagnostic = mockk<Diagnostic<JavaFileObject>>(name = innerDiagnosticName)
    val stackFrames = mockk<List<StackTraceElement>>()
    val timestamp = mockk<Instant>()

    val diagnostic = JavaRamDiagnostic(timestamp, innerDiagnostic, stackFrames)

    assertEquals(diagnostic.toString(), innerDiagnostic.toString())
  }
}