package io.ascopes.katana.compilertesting

import org.opentest4j.AssertionFailedError


/**
 * Base assertions for any form of compilation.
 *
 * @param C the compilation type.
 * @param target the compilation of type [C].
 * @author Ashley Scopes
 * @since 0.1.0
 */
abstract class CompilationAssertions<C : Compilation<*>, A : CompilationAssertions<C, A>>
internal constructor(target: C) : CommonAssertions<C, A>(target) {

  /**
   * Assert that the compilation succeeded.
   *
   * @return this assertion object for further checks.
   */
  fun isSuccessful() = apply {
    if (!target.result.isSuccess) {
      throw AssertionFailedError(
          UNEXPECTED_OUTCOME,
          SUCCESS,
          describeActualOutcomeType(),
          target.result.exception
      )
    }
  }

  /**
   * Assert that the compilation failed.
   *
   * This is not the same as the compiler throwing an unhandled exception.
   *
   * @return this assertion object for further checks.
   */
  fun isAFailure() = apply {
    if (!target.result.isFailure) {
      throw AssertionFailedError(
          UNEXPECTED_OUTCOME,
          FAILURE,
          describeActualOutcomeType(),
          target.result.exception
      )
    }
  }

  /**
   * Assert that the compiler panicked and raised an unhandled exception.
   *
   * @return an assertion object for the raised exception.
   */
  fun raisedAnUnhandledException(): ExceptionAssertions<Throwable> {
    if (!target.result.isException) {
      throw AssertionFailedError(
          UNEXPECTED_OUTCOME,
          EXCEPTION,
          describeActualOutcomeType()
      )
    }

    return ExceptionAssertions(target.result.exception!!)
  }

  private fun describeActualOutcomeType(): String {
    val type = target.result

    return when {
      type.isSuccess -> SUCCESS
      type.isFailure -> FAILURE
      type.isException -> EXCEPTION
      else -> throw UnsupportedOperationException(type.toString())
    }
  }

  private companion object {
    const val UNEXPECTED_OUTCOME = "Unexpected compilation outcome"

    const val SUCCESS = "success"
    const val FAILURE = "failure"
    const val EXCEPTION = "unhandled exception"
  }
}