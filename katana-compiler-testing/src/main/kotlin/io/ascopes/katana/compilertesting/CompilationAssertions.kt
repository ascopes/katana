package io.ascopes.katana.compilertesting

import org.opentest4j.AssertionFailedError


/**
 * Base assertions for any form of compilation.
 *
 * @param C the compilation type.
 * @author Ashley Scopes
 * @since 0.1.0
 */
abstract class CompilationAssertions<C, A> : CommonAssertions<C, A>
    where C : Compilation<*>,
          A : CompilationAssertions<C, A> {

  /**
   * @param target the target of the assertions to perform.
   */
  @Suppress("ConvertSecondaryConstructorToPrimary")
  internal constructor(target: C) : super(target)

  /**
   * Assert that the compilation succeeded.
   *
   * @return this assertion object for further checks.
   */
  fun isSuccessful() = apply {
    if (!target.result.isSuccess) {
      throw AssertionFailedError(
          UNEXPECTED_RESULT,
          SUCCESS,
          describeActualOutcomeType(),
          target.result.exception
      )
    }
  }

  /**
   * Assert that the compilation succeeded without any warnings.
   *
   * @return this assertion object for further checks.
   */
  abstract fun isSuccessfulWithoutWarnings(): A

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
          UNEXPECTED_RESULT,
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
          UNEXPECTED_RESULT,
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
    const val UNEXPECTED_RESULT = "Unexpected compilation result"

    const val SUCCESS = "success"
    const val FAILURE = "failure"
    const val EXCEPTION = "unhandled exception"
  }
}