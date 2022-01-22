package io.ascopes.katana.compilertesting.core

import java.util.regex.Pattern
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ExceptionAssertionsTest {
  @Test
  fun `isInstance{reified E}() succeeds for the same type`() {
    // Given
    val ex = BaseException("blah")
    val assertions = ExceptionAssertions(ex)

    // Then
    assertDoesNotThrow { assertions.isInstance<BaseException>() }
  }

  @Test
  fun `isInstance(Class{E}) succeeds for the same type`() {
    // Given
    val ex = BaseException("blah")
    val assertions = ExceptionAssertions(ex)

    // Then
    assertDoesNotThrow { assertions.isInstance(BaseException::class.java) }
  }

  @Test
  fun `isInstance{reified E}() succeeds for a subtype`() {
    // Given
    val ex = WontOpenException("blah")
    val assertions = ExceptionAssertions(ex)

    // Then
    assertDoesNotThrow { assertions.isInstance<BaseException>() }
  }

  @Test
  fun `isInstance(Class{E}) succeeds for a subtype`() {
    // Given
    val ex = WontOpenException("blah")
    val assertions = ExceptionAssertions(ex)

    // Then
    assertDoesNotThrow { assertions.isInstance(BaseException::class.java) }
  }

  @Test
  fun `isInstance{reified E}() fails for a super type`() {
    // Given
    val ex = BaseException("blah")
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.isInstance<WontOpenException>()
    }

    assertEquals("Unexpected exception type thrown", error.message)
    assertEquals(WontOpenException::class.java, error.expected.value)
    assertEquals(BaseException::class.java, error.actual.value)
    assertSame(ex, error.exceptionThatWasTested)
  }

  @Test
  fun `isInstance(Class{E}) fails for a supertype`() {
    // Given
    val ex = BaseException("blah")
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.isInstance<WontOpenException>()
    }

    assertEquals("Unexpected exception type thrown", error.message)
    assertEquals(WontOpenException::class.java, error.expected.value)
    assertEquals(BaseException::class.java, error.actual.value)
    assertSame(ex, error.exceptionThatWasTested)
  }

  @Test
  fun `hasNoMessage() succeeds if the exception message is null`() {
    // Given
    val ex = BaseException(null)
    val assertions = ExceptionAssertions(ex)

    // Then
    assertDoesNotThrow { assertions.hasNoMessage() }
  }

  @Test
  fun `hasNoMessage() fails if the exception message is not null`() {
    // Given
    val ex = BaseException("foobar")
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.hasNoMessage()
    }

    assertEquals("Exception had a message", error.message)
    assertEquals(null, error.expected.value)
    assertEquals("foobar", error.actual.value)
    assertSame(ex, error.exceptionThatWasTested)
  }

  @Test
  fun `hasMessage() succeeds if the exception message is not null`() {
    // Given
    val ex = BaseException("lorem ipsum")
    val assertions = ExceptionAssertions(ex)

    // Then
    assertDoesNotThrow { assertions.hasMessage() }
  }

  @Test
  fun `hasMessage() fails if the exception message is null`() {
    // Given
    val ex = BaseException(null)
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.hasMessage()
    }

    assertEquals("Exception had no message", error.message)
    assertEquals(String::class.java, error.expected.value)
    assertEquals(null, error.actual.value)
    assertSame(ex, error.exceptionThatWasTested)
  }

  @Test
  fun `hasMessageContent(String) fails if the exception message is null`() {
    // Given
    val ex = BaseException(null)
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.hasMessageContent("I am a potato")
    }

    assertEquals("Exception had no message", error.message)
    assertEquals(String::class.java, error.expected.value)
    assertEquals(null, error.actual.value)
    assertSame(ex, error.exceptionThatWasTested)
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `hasMessageContent(String, Boolean) fails if the exception message is null`(ignoreCase: Boolean) {
    // Given
    val ex = BaseException(null)
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.hasMessageContent("I am a potato", ignoreCase)
    }

    assertEquals("Exception had no message", error.message)
    assertEquals(String::class.java, error.expected.value)
    assertEquals(null, error.actual.value)
    assertSame(ex, error.exceptionThatWasTested)
  }

  @Test
  fun `hasMessageContent(String) succeeds if the message matches case sensitively`() {
    // Given
    val ex = BaseException("Potato")
    val assertions = ExceptionAssertions(ex)

    // Then
    assertDoesNotThrow { assertions.hasMessageContent("Potato") }
  }

  @ParameterizedTest
  @ValueSource(strings = ["POTATO", "potato", "KitKat"])
  fun `hasMessageContent(String) fails if the message does not match case sensitively`(message: String) {
    // Given
    val ex = BaseException("Potato")
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.hasMessageContent(message)
    }

    assertEquals("Unexpected exception message", error.message)
    assertEquals(message, error.expected.value)
    assertEquals("Potato", error.actual.value)
    assertSame(ex, error.exceptionThatWasTested)
  }

  @ParameterizedTest
  @ValueSource(strings = ["POTATO", "potato", "KitKat"])
  fun `hasMessageContent(String, false) fails if the message does not match case sensitively`(message: String) {
    // Given
    val ex = BaseException("Potato")
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.hasMessageContent(message, false)
    }

    assertEquals("Unexpected exception message", error.message)
    assertEquals(message, error.expected.value)
    assertEquals("Potato", error.actual.value)
    assertSame(ex, error.exceptionThatWasTested)
  }

  @Test
  fun `hasMessageContent(String, false) succeeds if the message matches case sensitively`() {
    // Given
    val ex = BaseException("Potato")
    val assertions = ExceptionAssertions(ex)

    // Then
    assertDoesNotThrow { assertions.hasMessageContent("Potato", false) }
  }

  @ParameterizedTest
  @ValueSource(strings = ["Potato", "POTATO", "potato"])
  fun `hasMessageContent(String, true) succeeds if the message matches case insensitively`(message: String) {
    // Given
    val ex = BaseException("Potato")
    val assertions = ExceptionAssertions(ex)

    // Then
    assertDoesNotThrow { assertions.hasMessageContent(message, true) }
  }

  @Test
  fun `hasMessageContent(String, true) fails if the message does not match case insensitively`() {
    // Given
    val ex = BaseException("Potato")
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.hasMessageContent("Cheese", false)
    }

    assertEquals("Unexpected exception message", error.message)
    assertEquals("Cheese", error.expected.value)
    assertEquals("Potato", error.actual.value)
    assertSame(ex, error.exceptionThatWasTested)
  }

  @Test
  fun `hasMessageMatching(String) fails if the exception message is null`() {
    // Given
    val ex = BaseException(null)
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.hasMessageMatching(".*")
    }

    assertEquals("Exception had no message", error.message)
    assertEquals(String::class.java, error.expected.value)
    assertEquals(null, error.actual.value)
    assertSame(ex, error.exceptionThatWasTested)
  }

  @Test
  fun `hasMessageMatching(String, vararg RegexOption) fails if the exception message is null`() {
    // Given
    val ex = BaseException(null)
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.hasMessageMatching(".*", RegexOption.MULTILINE)
    }

    assertEquals("Exception had no message", error.message)
    assertEquals(String::class.java, error.expected.value)
    assertEquals(null, error.actual.value)
    assertSame(ex, error.exceptionThatWasTested)
  }

  @Test
  fun `hasMessageMatching(String, vararg Int) fails if the exception message is null`() {
    // Given
    val ex = BaseException(null)
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.hasMessageMatching(".*", Pattern.UNICODE_CASE)
    }

    assertEquals("Exception had no message", error.message)
    assertEquals(String::class.java, error.expected.value)
    assertEquals(null, error.actual.value)
    assertSame(ex, error.exceptionThatWasTested)
  }

  @Test
  fun `hasMessageMatching(String) fails if no match is found`() {
    // Given
    val ex = BaseException("Hello, Kotlin!")
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.hasMessageMatching("^Hello, Planet/World!$")
    }

    assertEquals(
        """
          Exception message did not match pattern /^Hello, Planet\/World!${'$'}/
          Options: <none>
          Message: <Hello, Kotlin!>
        """.trimIndent(),
        error.message
    )
  }

  @Test
  fun `hasMessageMatching(String, vararg RegexOption) fails if no match is found`() {
    // Given
    val ex = BaseException("Hello, Kotlin!")
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.hasMessageMatching("^Hello, Planet/World!$", RegexOption.IGNORE_CASE)
    }

    assertEquals(
        """
          Exception message did not match pattern /^Hello, Planet\/World!${'$'}/
          Options: <IGNORE_CASE>
          Message: <Hello, Kotlin!>
        """.trimIndent(),
        error.message
    )
  }

  @Test
  fun `hasMessageMatching(String, vararg Int) fails if no match is found`() {
    // Given
    val ex = BaseException("Hello, Kotlin!")
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.hasMessageMatching("^Hello, Planet/World!$", Pattern.DOTALL, Pattern.LITERAL)
    }

    assertEquals(
        """
          Exception message did not match pattern /^Hello, Planet\/World!${'$'}/
          Options: <DOT_MATCHES_ALL, LITERAL>
          Message: <Hello, Kotlin!>
        """.trimIndent(),
        error.message
    )
  }

  @Test
  fun `hasMessageMatching(String) succeeds if a match is found`() {
    // Given
    val ex = BaseException("Hello, Kotlin!")
    val assertions = ExceptionAssertions(ex)

    // Then
    assertDoesNotThrow {
      assertions.hasMessageMatching("^Hello, Kotlin!$")
    }
  }

  @Test
  fun `hasMessageMatching(String, vararg RegexOption) succeeds if a match is found`() {
    // Given
    val ex = BaseException("Hello, Kotlin!")
    val assertions = ExceptionAssertions(ex)

    // Then
    assertDoesNotThrow {
      assertions.hasMessageMatching("^Hello, kotlin!$", RegexOption.IGNORE_CASE)
    }
  }

  @Test
  fun `hasMessageMatching(String, vararg Int) succeeds if a match is found`() {
    // Given
    val ex = BaseException("Hello\nKotlin!")
    val assertions = ExceptionAssertions(ex)

    // Then
    assertDoesNotThrow {
      assertions.hasMessageMatching("^Hello.Kotlin!$", RegexOption.DOT_MATCHES_ALL)
    }
  }

  @Test
  fun `hasMessageMatching(Pattern) fails if the exception message is null`() {
    // Given
    val ex = BaseException(null)
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.hasMessageMatching(Pattern.compile(".*"))
    }

    assertEquals("Exception had no message", error.message)
    assertEquals(String::class.java, error.expected.value)
    assertEquals(null, error.actual.value)
    assertSame(ex, error.exceptionThatWasTested)
  }

  @Test
  fun `hasMessageMatching(Pattern) fails if the exception message does not match`() {
    // Given
    val ex = BaseException("FOObAr")
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.hasMessageMatching(Pattern.compile("foobar", Pattern.LITERAL))
    }

    assertEquals(
        """
          Exception message did not match pattern /foobar/
          Options: <LITERAL>
          Message: <FOObAr>
        """.trimIndent(),
        error.message
    )
  }

  @Test
  fun `hasMessageMatching(Pattern) succeeds if the exception message matches`() {
    // Given
    val ex = BaseException("FOObAr")
    val assertions = ExceptionAssertions(ex)

    // Then
    assertDoesNotThrow {
      assertions.hasMessageMatching(Pattern.compile("foobar", Pattern.CASE_INSENSITIVE))
    }
  }

  @Test
  fun `hasMessageMatching(Regex) fails if the exception message is null`() {
    // Given
    val ex = BaseException(null)
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.hasMessageMatching(Regex(".*"))
    }

    assertEquals("Exception had no message", error.message)
    assertEquals(String::class.java, error.expected.value)
    assertEquals(null, error.actual.value)
    assertSame(ex, error.exceptionThatWasTested)
  }

  @Test
  fun `hasMessageMatching(Regex) fails if the exception message does not match`() {
    // Given
    val ex = BaseException("FOObAz")
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.hasMessageMatching(Regex("foobaz", RegexOption.LITERAL))
    }

    assertEquals(
        """
          Exception message did not match pattern /foobaz/
          Options: <LITERAL>
          Message: <FOObAz>
        """.trimIndent(),
        error.message
    )
  }

  @Test
  fun `hasMessageMatching(Regex) succeeds if the exception message matches`() {
    // Given
    val ex = BaseException("FOObAr")
    val assertions = ExceptionAssertions(ex)

    // Then
    assertDoesNotThrow {
      assertions.hasMessageMatching(Regex("foobar", RegexOption.IGNORE_CASE))
    }
  }

  @Test
  fun `hasNoCause() succeeds if there is no cause`() {
    // Given
    val ex = ExceptionWithCause("this has no cause", null)
    val assertions = ExceptionAssertions(ex)

    // Then
    assertDoesNotThrow { assertions.hasNoCause() }
  }

  @Test
  fun `hasNoCause() fails if there is a cause`() {
    // Given
    val cause = Throwable("Bang!")
    val ex = ExceptionWithCause("this has a cause", cause)
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.hasNoCause()
    }

    assertEquals("Did not expect an exception cause to be present", error.message)
    assertEquals(null, error.expected.value)
    assertEquals(ex.cause, error.actual.value)
    assertSame(ex, error.exceptionThatWasTested)
  }

  @Test
  fun `hasCause() succeeds if there is a cause`() {
    // Given
    val cause = Throwable("Bang!")
    val ex = ExceptionWithCause("this has a cause", cause)
    val assertions = ExceptionAssertions(ex)

    // Then
    assertDoesNotThrow { assertions.hasCause() }
  }

  @Test
  fun `hasCause() returns assertions for the cause`() {
    // Given
    val cause = Throwable("Bang!")
    val ex = ExceptionWithCause("this has a cause", cause)
    val assertions = ExceptionAssertions(ex)

    // When
    val causeAssertions = assertions.hasCause()

    // Then
    causeAssertions.satisfies {
      assertSame(cause, it, "cause did not match the expected exception")
    }
  }

  @Test
  fun `hasCause() fails if there is no cause`() {
    // Given
    val ex = ExceptionWithCause("this has no cause", null)
    val assertions = ExceptionAssertions(ex)

    // Then
    val error = assertThrows<ExceptionAssertionFailedError> {
      assertions.hasCause()
    }

    assertEquals("Expected an exception cause to be present", error.message)
    assertSame(ex, error.exceptionThatWasTested)
    assertFalse(error.isExpectedDefined)
    assertFalse(error.isActualDefined)
  }

  open class BaseException(message: String?) : Exception(message)
  open class WontOpenException(what: String) : BaseException(what)
  open class ExceptionWithCause(message: String, cause: Throwable?) : Exception(message, cause)

}