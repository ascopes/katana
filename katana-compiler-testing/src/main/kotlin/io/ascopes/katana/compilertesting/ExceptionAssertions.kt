package io.ascopes.katana.compilertesting

import java.util.regex.Pattern

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
   * @param E the exception type to attempt to cast to.
   * @return the exception assertions for the cast type.
   */
  inline fun <reified E : Exception> isInstance() = this.isInstance(E::class.java)

  /**
   * Java-style instance check. Will fail if the exception is not an instance of the provided
   * exception type.
   *
   * Any additional assertions chained onto this call will be cast to the checked type
   * provided as a type parameter, to allow implicit down-casting.
   *
   * @param E the exception type to attempt to cast to.
   * @param type the class descriptor for the type to cast to.
   * @return the exception assertions for the cast type.
   */
  fun <E : Throwable> isInstance(type: Class<E>): ExceptionAssertions<E> {
    if (!type.isInstance(target)) {
      throw ExceptionAssertionFailedError(
          "Unexpected exception type thrown",
          type,
          target::class.java,
          target
      )
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
      throw ExceptionAssertionFailedError(
          "Exception had a message",
          null,
          target.message,
          target
      )
    }
  }

  /**
   * Assert that the exception has a non-null message.
   *
   * @return this assertion object for further checks.
   */
  fun hasMessage() {
    if (target.message == null) {
      throw ExceptionAssertionFailedError(
          "Exception had no message",
          String::class.java,
          null,
          target
      )
    }
  }

  /**
   * Assert that the exception has the given message.
   *
   * @param message the message to check for.
   * @param ignoreCase `true` to ignore character case, and `false` (default) to consider it.
   * @return this assertion object for further checks.
   */
  @JvmOverloads
  fun hasMessageContent(message: String, ignoreCase: Boolean = false) = apply {
    hasMessage()

    if (!target.message.equals(message, ignoreCase = ignoreCase)) {
      throw ExceptionAssertionFailedError(
          "Unexpected exception message",
          message,
          target.message,
          target
      )
    }
  }

  /**
   * Assert that the exception has a message matching the given pattern.
   *
   * @param pattern the pattern to search for.
   * @return this assertion object for further checks.
   */
  fun hasMessageMatching(pattern: String): ExceptionAssertions<T> {
    return this.hasMessageMatching(pattern.toRegex())
  }

  /**
   * Assert that the exception has a message matching the given pattern.
   *
   * @param pattern the pattern to search for.
   * @param flags the [Pattern] flags to allow.
   * @return this assertion object for further checks.
   */
  fun hasMessageMatching(pattern: String, vararg flags: Int): ExceptionAssertions<T> {
    val combinedFlags = flags.fold(0) { a, b -> a or b }
    return this.hasMessageMatching(pattern.toPattern(combinedFlags))
  }

  /**
   * Assert that the exception has a message matching the given pattern.
   *
   * @param pattern the pattern to search for.
   * @param flags the [RegexOption] flags to allow.
   * @return this assertion object for further checks.
   */
  fun hasMessageMatching(pattern: String, vararg flags: RegexOption): ExceptionAssertions<T> {
    return this.hasMessageMatching(Regex(pattern, flags.toSet()))
  }

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

    if (pattern.matchEntire(target.message!!) == null) {
      val patternStr = pattern.pattern.replace("/", "\\/")
      val patternOpts = pattern.options.map { it.name }.sorted().joinToString()
          .ifEmpty { "none" }

      val message = StringBuilder()
          .append("Exception message did not match pattern /")
          .append(patternStr)
          .appendLine("/")
          .append("Options: <")
          .append(patternOpts)
          .appendLine(">")
          .append("Message: <")
          .append(target.message)
          .append(">")

      throw ExceptionAssertionFailedError(message.toString(), target)
    }
  }

  /**
   * Assert that the exception has no cause set.
   *
   * @return this assertion object for further checks.
   */
  fun hasNoCause() = apply {
    if (target.cause != null) {
      throw ExceptionAssertionFailedError(
          "Did not expect an exception cause to be present",
          null,
          target.cause,
          target
      )
    }
  }

  /**
   * Assert that the exception has a cause.
   *
   * @return the assertion object for the cause.
   */
  fun hasCause(): ExceptionAssertions<Throwable> {
    if (target.cause == null) {
      throw ExceptionAssertionFailedError(
          "Expected an exception cause to be present",
          target
      )
    }

    return ExceptionAssertions(target.cause!!)
  }
}