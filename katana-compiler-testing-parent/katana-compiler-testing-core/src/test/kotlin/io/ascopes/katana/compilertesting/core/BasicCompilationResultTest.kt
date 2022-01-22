package io.ascopes.katana.compilertesting.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BasicCompilationResultTest {
  @Test
  fun `initializing the result with 'true' provides a successful result`() {
    // Given
    val result = BasicCompilationResult(true)

    // Then
    assertTrue(result.isSuccess, "isSuccess was false unexpectedly")
    assertFalse(result.isFailure, "isFailure was true unexpectedly")
    assertFalse(result.isException, "isException was true unexpectedly")
    assertNull(result.exception, "exception was non-null unexpectedly")
    assertEquals("success", result.toString())
  }

  @Test
  fun `initializing the result with 'false' provides a failed result`() {
    // Given
    val result = BasicCompilationResult(false)

    // Then
    assertFalse(result.isSuccess, "isSuccess was true unexpectedly")
    assertTrue(result.isFailure, "isFailure was false unexpectedly")
    assertFalse(result.isException, "isException was true unexpectedly")
    assertNull(result.exception, "exception was non-null unexpectedly")
    assertEquals("failure", result.toString())
  }


  @Test
  fun `initializing the result with an exception provides an exceptional result`() {
    // Given
    val exception = RuntimeException("Stuff's broke, yo")
        .apply { fillInStackTrace() }

    val result = BasicCompilationResult(exception)

    // Then
    assertFalse(result.isSuccess, "isSuccess was true unexpectedly")
    assertFalse(result.isFailure, "isFailure was true unexpectedly")
    assertTrue(result.isException, "isException was false unexpectedly")
    assertSame(exception, result.exception, "exception had an unexpected value")
    assertEquals(
        "unhandled exception thrown (RuntimeException: Stuff's broke, yo)",
        result.toString()
    )
  }
}