package io.ascopes.katana.compilertesting

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.function.Predicate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.opentest4j.AssertionFailedError
import org.opentest4j.IncompleteExecutionException

class CommonAssertionsTest {
  @Test
  fun `target is initialized as a field`() {
    // Given
    val target = Something()

    // When
    val assertions = SomeAssertions(target)

    // Then
    assertSame(target, assertions.getTarget())
  }

  @Test
  fun `matches(Predicate) invokes the predicate with the given target`() {
    // Given
    val target = Something()
    val assertions = SomeAssertions(target)
    val predicate = mockk<Predicate<Something>>()
    every { predicate.test(any()) } returns true

    // Then
    assertions.matches(predicate)
    verify { predicate.test(target) }
  }

  @Test
  fun `matches(String, Predicate) invokes the predicate with the given target`() {
    // Given
    val target = Something()
    val assertions = SomeAssertions(target)
    val predicate = mockk<Predicate<Something>>()
    every { predicate.test(any()) } returns true

    // Then
    assertions.matches("foobar", predicate)
    verify { predicate.test(target) }
  }

  @Test
  fun `matches(Predicate) throws an exception for a false predicate`() {
    // Given
    val target = Something()
    val assertions = SomeAssertions(target)
    val predicate = mockk<Predicate<Something>>(relaxed = true)
    every { predicate.test(any()) } returns false

    // Then
    val ex = assertThrows<AssertionError> { assertions.matches(predicate) }
    assertEquals("The given predicate returned false", ex.message)

    verify { predicate.test(target) }
  }

  @Test
  fun `matches(String, Predicate) throws an exception for a false predicate`() {
    // Given
    val target = Something()
    val assertions = SomeAssertions(target)
    val predicate = mockk<Predicate<Something>>(relaxed = true)
    every { predicate.test(any()) } returns false

    // Then
    val ex = assertThrows<AssertionError> { assertions.matches("my message here", predicate) }
    assertEquals("my message here", ex.message)

    verify { predicate.test(target) }
  }

  @Test
  fun `satisfies(Expectation{T}) succeeds if nothing is thrown`() {
    // Given
    val target = Something()
    val assertions = SomeAssertions(target)

    // Then
    assertDoesNotThrow {
      assertions.satisfies {
        assertEquals(1, 1)
      }
    }
  }

  @Test
  fun `satisfies(Expectation{T}) fails if an AssertionError is thrown`() {
    // Given
    val target = Something()
    val assertions = SomeAssertions(target)

    // Then
    val error = assertThrows<AssertionError> {
      assertions.satisfies {
        assertEquals(1, 2)
      }
    }

    assertInstanceOf(AssertionFailedError::class.java, error)
    val castError = error as AssertionFailedError
    assertEquals(1, castError.expected.value)
    assertEquals(2, castError.actual.value)
  }

  @Test
  fun `satisfies(Expectation{T}) errors if any other exception is thrown`() {
    // Background
    assumeFalse(
        AssertionError::class.java.isAssignableFrom(IncompleteExecutionException::class.java),
        "IncompleteExecutionException appears to subclass AssertionError, " +
            "so this test is not valid!"
    )

    // Given
    val target = Something()
    val assertions = SomeAssertions(target)
    val ex = Throwable("Wahh!")


    // Then
    val error = assertThrows<IncompleteExecutionException> {
      assertions.satisfies { throw ex }
    }

    assertEquals("Unexpected exception thrown", error.message)
    assertSame(ex, error.cause)
  }

  class Something
  class SomeAssertions(target: Something) : CommonAssertions<Something, SomeAssertions>(target) {
    fun getTarget() = target
  }
}