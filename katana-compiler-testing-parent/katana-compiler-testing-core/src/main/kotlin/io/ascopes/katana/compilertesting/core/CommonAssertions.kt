package io.ascopes.katana.compilertesting.core

import java.util.function.Predicate
import org.opentest4j.AssertionFailedError
import org.opentest4j.IncompleteExecutionException

/**
 * Common assertion methods.
 *
 * @param T the type of object that assertions are being performed on.
 * @param A the implementation of [CommonAssertions] that is being defined. This enables methods
 *    to facilitate type-safe chaining of the implementation internally.
 * @author Ashley Scopes
 * @since 0.1.0
 */
@Suppress("unused")
abstract class CommonAssertions<T, A>
  : PolymorphicTypeSafeBuilder<A>
    where A : CommonAssertions<T, A> {

  protected val target: T

  /**
   * @param target the target of the assertions to perform.
   */
  @Suppress("ConvertSecondaryConstructorToPrimary")
  protected constructor(target: T) {
    this.target = target
  }

  /**
   * Assert that the target matches a given predicate.
   *
   * @param predicate the predicate to match.
   * @return this assertion object for further checks.
   */
  fun matches(predicate: Predicate<T>) = this
      .matches("The given predicate returned false", predicate)

  /**
   * Assert that the target matches a given predicate.
   *
   * @param message the message to show when the predicate fails.
   * @param predicate the predicate to match.
   * @return this assertion object for further checks.
   */
  fun matches(message: String, predicate: Predicate<T>) = apply {
    predicate.test(target) || throw AssertionFailedError(message)
  }

  /**
   * Run some code on the given target, and catch any exceptions it throws.
   *
   * If an [AssertionError] or similar is thrown, the test is marked as failed.
   * If any other [Throwable] is thrown, then the test is marked as erroneous.
   *
   * @param expectations the expectations to check.
   * @return this assertion object for further checks.
   */
  fun satisfies(expectations: Expectations<T>) = apply {
    try {
      expectations.invoke(target)
    } catch (ex: AssertionError) {
      // Rethrow.
      throw ex
    } catch (ex: Throwable) {
      throw IncompleteExecutionException("Unexpected exception thrown", ex)
    }
  }

  /**
   * Functional interface for an expectation routine.
   */
  fun interface Expectations<T> {
    @Throws(Exception::class)
    operator fun invoke(target: T)
  }
}