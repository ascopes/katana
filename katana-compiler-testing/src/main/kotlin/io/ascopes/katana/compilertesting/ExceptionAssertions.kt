package io.ascopes.katana.compilertesting

import java.util.regex.Pattern
import org.opentest4j.AssertionFailedError

/**
 * Assertions for a given exception.
 *
 * @param T the exception type.
 * @author Ashley Scopes
 * @since 0.1.0
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class ExceptionAssertions<T : Throwable>
  : CommonAssertions<T, ExceptionAssertions<T>> {

  /**
   * @param target the target of the assertions to perform.
   */
  @Suppress("ConvertSecondaryConstructorToPrimary")
  internal constructor(target: T) : super(target)

  /**
   * Kotlin-style instance check. Will fail if the exception is not an instance of the provided
   * exception type.
   *
   * Any additional assertions chained onto this call will be cast to the checked type
   * provided as a type parameter, to allow implicit down-casting.
   *
   * @param U the exception type to attempt to cast to.
   * @return the exception assertions for the cast type.
   */
  inline fun <reified U : Exception> isInstance() = this.isInstance(U::class.java)

  /**
   * Java-style instance check. Will fail if the exception is not an instance of the provided
   * exception type.
   *
   * Any additional assertions chained onto this call will be cast to the checked type
   * provided as a type parameter, to allow implicit down-casting.
   *
   * @param U the exception type to attempt to cast to.
   * @param type the class descriptor for the type to cast to.
   * @return the exception assertions for the cast type.
   */
  fun <U : Throwable> isInstance(type: Class<U>): ExceptionAssertions<U> {
    if (!type.isInstance(target)) {
      throw AssertionFailedError("Unexpected exception type thrown", type, target::class.java)
          .apply { addSuppressed(target) }
    }

    return ExceptionAssertions(type.cast(target))
  }

  /**
   * Assert that the exception has a null message.
   *
   * @return this assertion object for further checks.
   */
  fun hasNoMessage() = apply {
    if (target.message != null) {
      throw AssertionFailedError("Unexpected exception message", null, target.message)
          .apply { addSuppressed(target) }
    }
  }

  /**
   * Assert that the exception has a non-null message.
   *
   * @return this assertion object for further checks.
   */
  fun hasMessage() {
    if (target.message != null) {
      throw AssertionFailedError("Exception had no message", String::class.java, null)
          .apply { addSuppressed(target) }
    }
  }

  /**
   * Assert that the exception has the given message.
   *
   * @param message the message to check for.
   * @return this assertion object for further checks.
   */
  fun hasMessageContent(message: String) = apply {
    hasMessage()

    if (target.message != message) {
      throw AssertionFailedError("Unexpected exception message", message, target.message)
          .apply { addSuppressed(target) }
    }
  }

  /**
   * Assert that the exception has a message matching the given pattern.
   *
   * @param pattern the pattern to search for.
   * @return this assertion object for further checks.
   */
  fun hasMessageMatching(pattern: String) = hasMessageMatching(pattern.toRegex())

  /**
   * Assert that the exception has a message matching the given pattern.
   *
   * @param pattern the pattern to search for.
   * @return this assertion object for further checks.
   */
  fun hasMessageMatching(pattern: Pattern) = hasMessageMatching(pattern.toRegex())

  /**
   * Assert that the exception has a message matching the given pattern.
   *
   * @param pattern the pattern to search for.
   * @return this assertion object for further checks.
   */
  fun hasMessageMatching(pattern: Regex) = apply {
    hasMessage()

    pattern.matchEntire(target.message!!)
        ?: throw AssertionFailedError("Exception message did not match pattern $pattern")
            .apply { addSuppressed(target) }
  }

  /**
   * Assert that the exception has no cause set.
   *
   * @return this assertion object for further checks.
   */
  fun hasNoCause() = apply {
    if (target.cause != null) {
      throw AssertionFailedError("Unexpected exception cause", "no cause", target.cause)
          .apply { addSuppressed(target) }
    }
  }

  /**
   * Assert that the exception has a cause.
   *
   * @return the assertion object for the cause.
   */
  fun hasCause(): ExceptionAssertions<Throwable> {
    if (target.cause == null) {
      throw AssertionFailedError("No exception cause found", "a valid cause", "no cause")
          .apply { addSuppressed(target) }
    }

    return ExceptionAssertions(target.cause!!)
  }
}