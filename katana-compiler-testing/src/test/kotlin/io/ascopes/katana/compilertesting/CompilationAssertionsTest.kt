package io.ascopes.katana.compilertesting

import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.opentest4j.AssertionFailedError

class CompilationAssertionsTest {
  @Test
  fun `isSuccessful succeeds for a successful result`() {
    // Given
    val result = BasicCompilationResult(true)
    val compilation = mockk<Compilation<BasicCompilationResult>>()
    every { compilation.result } returns result
    val assertions = SomeCompilationAssertions(compilation)

    assertDoesNotThrow {
      assertions
          .isSuccessful()
          .isSuccessful()
          .isSuccessful()
    }
  }

  @Test
  fun `isSuccessful throws an AssertionError for a failed result`() {
    // Given
    val result = BasicCompilationResult(false)
    val compilation = mockk<Compilation<BasicCompilationResult>>()
    every { compilation.result } returns result
    val assertions = SomeCompilationAssertions(compilation)

    val error = assertThrows<AssertionFailedError> {
      assertions.isSuccessful()
    }

    assertEquals("Unexpected compilation result", error.message)
    assertEquals("success", error.expected.value)
    assertEquals("failure", error.actual.value)
    assertNull(error.cause)
  }

  @Test
  fun `isSuccessful throws an AssertionError for an exceptional result`() {
    // Given
    val message = UUID.randomUUID().toString()
    val exception = RuntimeException(message)
        .apply { fillInStackTrace() }
    val result = BasicCompilationResult(exception)
    val compilation = mockk<Compilation<BasicCompilationResult>>()
    every { compilation.result } returns result
    val assertions = SomeCompilationAssertions(compilation)

    val error = assertThrows<AssertionFailedError> {
      assertions.isSuccessful()
    }

    assertEquals("Unexpected compilation result", error.message)
    assertEquals("success", error.expected.value)
    assertEquals("unhandled exception", error.actual.value)
    assertSame(exception, error.cause)
  }

  @Test
  fun `isAFailure succeeds for a failure result`() {
    // Given
    val result = BasicCompilationResult(false)
    val compilation = mockk<Compilation<BasicCompilationResult>>()
    every { compilation.result } returns result
    val assertions = SomeCompilationAssertions(compilation)

    assertDoesNotThrow {
      assertions
          .isAFailure()
          .isAFailure()
          .isAFailure()
    }
  }

  @Test
  fun `isFailure throws an AssertionError for a successful result`() {
    // Given
    val result = BasicCompilationResult(true)
    val compilation = mockk<Compilation<BasicCompilationResult>>()
    every { compilation.result } returns result
    val assertions = SomeCompilationAssertions(compilation)

    val error = assertThrows<AssertionFailedError> {
      assertions.isAFailure()
    }

    assertEquals("Unexpected compilation result", error.message)
    assertEquals("failure", error.expected.value)
    assertEquals("success", error.actual.value)
    assertNull(error.cause)
  }

  @Test
  fun `isFailure throws an AssertionError for an exceptional result`() {
    // Given
    val message = UUID.randomUUID().toString()
    val exception = RuntimeException(message)
        .apply { fillInStackTrace() }
    val result = BasicCompilationResult(exception)
    val compilation = mockk<Compilation<BasicCompilationResult>>()
    every { compilation.result } returns result
    val assertions = SomeCompilationAssertions(compilation)

    val error = assertThrows<AssertionFailedError> {
      assertions.isAFailure()
    }

    assertEquals("Unexpected compilation result", error.message)
    assertEquals("failure", error.expected.value)
    assertEquals("unhandled exception", error.actual.value)
    assertSame(exception, error.cause)
  }

  @Test
  fun `raisedAnUnhandledException succeeds for an exceptional result`() {
    // Given
    val exception = RuntimeException("Fizz, bang!")
        .apply { fillInStackTrace() }
    val result = BasicCompilationResult(exception)
    val compilation = mockk<Compilation<BasicCompilationResult>>()
    every { compilation.result } returns result
    val assertions = SomeCompilationAssertions(compilation)

    assertDoesNotThrow {
      assertions
          .raisedAnUnhandledException()
          .hasMessageContent("Fizz, bang!")
    }
  }

  @Test
  fun `raisedAnUnhandledException throws an AssertionError for a successful result`() {
    // Given
    val result = BasicCompilationResult(true)
    val compilation = mockk<Compilation<BasicCompilationResult>>()
    every { compilation.result } returns result
    val assertions = SomeCompilationAssertions(compilation)

    val error = assertThrows<AssertionFailedError> {
      assertions.raisedAnUnhandledException()
    }

    assertEquals("Unexpected compilation result", error.message)
    assertEquals("unhandled exception", error.expected.value)
    assertEquals("success", error.actual.value)
    assertNull(error.cause)
  }

  @Test
  fun `raisedAnUnhandledException throws an AssertionError for a failed result`() {
    // Given
    val result = BasicCompilationResult(false)
    val compilation = mockk<Compilation<BasicCompilationResult>>()
    every { compilation.result } returns result
    val assertions = SomeCompilationAssertions(compilation)

    val error = assertThrows<AssertionFailedError> {
      assertions.raisedAnUnhandledException()
    }

    assertEquals("Unexpected compilation result", error.message)
    assertEquals("unhandled exception", error.expected.value)
    assertEquals("failure", error.actual.value)
    assertNull(error.cause)
  }

  internal class SomeCompilationAssertions<T>(target: T)
    : CompilationAssertions<T, SomeCompilationAssertions<T>>(target)
      where T : Compilation<BasicCompilationResult> {
    override fun isSuccessfulWithoutWarnings(): SomeCompilationAssertions<T> = TODO()
  }
}