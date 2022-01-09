package io.ascopes.katana.compilertesting.compilation


/**
 * Description of the outcome of a compilation.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
class JavaCompilationOutcomeType {
  private val innerOutcomeType: InnerOutcomeType

  internal constructor(success: Boolean) {
    this.innerOutcomeType = if (success) {
      SuccessOutcomeType
    } else {
      FailureOutcomeType
    }
  }

  internal constructor(exception: Exception) {
    this.innerOutcomeType = ExceptionOutcomeType(exception)
  }

  /**
   * `true` if the compilation was a success.
   */
  val isSuccess: Boolean
      get() = this.innerOutcomeType is SuccessOutcomeType

  /**
   * `true` if the compilation failed due to the input.
   */
  val isFailure: Boolean
    get() = this.innerOutcomeType is FailureOutcomeType

  /**
   * `true` if the compilation raised an unhandled exception.
   */
  val isException: Boolean
    get() = this.innerOutcomeType is ExceptionOutcomeType

  /**
   * The exception that was thrown, if the compilation raised an unhandled exception.
   * Otherwise, this is always null.
   */
  val exception: Exception?
    get() = (this.innerOutcomeType as? ExceptionOutcomeType)?.cause

  private sealed interface InnerOutcomeType

  private object SuccessOutcomeType : InnerOutcomeType

  private object FailureOutcomeType : InnerOutcomeType

  @JvmInline
  private value class ExceptionOutcomeType(val cause: Exception) : InnerOutcomeType
}