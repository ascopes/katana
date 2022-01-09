package io.ascopes.katana.compilertesting.assertions

import io.ascopes.katana.compilertesting.compilation.JavaCompilation
import io.ascopes.katana.compilertesting.files.JavaCompilationModuleMode
import org.opentest4j.AssertionFailedError

/**
 * Assertions for the result of a Java compilation.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class JavaCompilationAssertions internal constructor(
    target: JavaCompilation
) : CommonAssertions<JavaCompilation>(target) {
  /**
   * Get an assertion object for the diagnostics produced by the compilation.
   *
   * @return the assertion object for the diagnostics.
   */
  fun diagnostics() = JavaDiagnosticsAssertions(target.diagnostics)

  /**
   * Get an assertion object for the files produced by the compilation.
   *
   * @return the assertion object for the files.
   */
  fun files() = JavaRamFileManagerAssertions(target.fileManager)

  /**
   * Assert that the compiler ran in the "legacy" single-project mode.
   *
   * @return this assertion object for further checks.
   */
  fun ranInLegacyMode() {
    assert(target.fileManager.moduleMode == null) {
      "No legacy/multi-module sources were provided, so the mode has not been set."
    }

    if (target.fileManager.moduleMode != JavaCompilationModuleMode.LEGACY) {
      throw AssertionFailedError(
          "Expected the compiler to run in legacy mode",
          JavaCompilationModuleMode.LEGACY,
          target.fileManager.moduleMode
      )
    }
  }

  /**
   * Assert that the compiler ran in the "legacy" single-project mode.
   *
   * @return this assertion object for further checks.
   */
  fun ranInMultiModuleMode() {
    assert(target.fileManager.moduleMode == null) {
      "No legacy/multi-module sources were provided, so the mode has not been set."
    }

    if (target.fileManager.moduleMode != JavaCompilationModuleMode.MULTI_MODULE) {
      throw AssertionFailedError(
          "Expected the compiler to run in multi-module mode",
          JavaCompilationModuleMode.LEGACY,
          target.fileManager.moduleMode
      )
    }
  }

  /**
   * Assert that the compilation succeeded.
   *
   * @return this assertion object for further checks.
   */
  fun isSuccessful() = apply {
    if (!target.type.isSuccess) {
      throw AssertionFailedError(
          UNEXPECTED_OUTCOME,
          SUCCESS,
          describeActualOutcomeType(),
          target.type.exception
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
    if (!target.type.isFailure) {
      throw AssertionFailedError(
          UNEXPECTED_OUTCOME,
          FAILURE,
          describeActualOutcomeType(),
          target.type.exception
      )
    }
  }

  /**
   * Assert that the compiler panicked and raised an unhandled exception of some description.
   *
   * @return an assertion object for the raised exception.
   */
  fun raisedAnUnhandledException(): ExceptionAssertions<Throwable> {
    if (!target.type.isException) {
      throw AssertionFailedError(
          UNEXPECTED_OUTCOME,
          EXCEPTION,
          describeActualOutcomeType()
      )
    }

    return ExceptionAssertions(target.type.exception!!)
  }

  private fun describeActualOutcomeType(): String {
    val type = target.type

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