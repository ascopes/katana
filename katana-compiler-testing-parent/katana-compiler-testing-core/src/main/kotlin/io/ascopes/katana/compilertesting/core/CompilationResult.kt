package io.ascopes.katana.compilertesting.core

/**
 * Description of the result of a compilation.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
interface CompilationResult {
  /**
   * `true` if the compilation succeeded.
   */
  val isSuccess: Boolean

  /**
   * `true` if the compilation failed.
   */
  val isFailure: Boolean

  /**
   * `true` if the result was an unhandled exception.
   */
  val isException: Boolean

  /**
   * If [isException] is true, this will be the exception that is thrown.
   *
   * For any other cases, this will be `null`.
   */
  val exception: Exception?
}