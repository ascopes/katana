package io.ascopes.katana.compilertesting

import java.util.function.Consumer
import java.util.function.Predicate
import org.opentest4j.AssertionFailedError

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
  internal constructor(target: T) {
    this.target = target
  }

  /**
   * Assert that the target matches a given predicate.
   *
   * @param predicate the predicate to match.
   * @return this assertion object for further checks.
   */
  @JvmOverloads
  fun matches(message: String? = null, predicate: Predicate<T>) = apply {
    if (!predicate.test(target)) {
      throw AssertionFailedError(
          message ?: "The given predicate was not matched"
      )
    }
  }

  /**
   * Assert that the target satisfies a given procedure.
   *
   * Any assertion errors should be raised directly.
   *
   * @param assertions the procedure containing assertions to invoke on the target object.
   * @return this assertion object for further checks.
   */
  fun satisfies(assertions: Consumer<T>) = apply {
    assertions.accept(target)
  }
}