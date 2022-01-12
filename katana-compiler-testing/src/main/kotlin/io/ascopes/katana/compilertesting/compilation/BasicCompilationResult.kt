package io.ascopes.katana.compilertesting.compilation


/**
 * Basic description of the outcome of a compilation for a compiler that does
 * not provide other additional outcomes.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
internal class BasicCompilationResult : CompilationResult {
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
  override val isSuccess: Boolean
      get() = this.innerOutcomeType is SuccessOutcomeType

  /**
   * `true` if the compilation failed due to the input.
   */
  override val isFailure: Boolean
    get() = this.innerOutcomeType is FailureOutcomeType

  /**
   * `true` if the compilation raised an unhandled exception.
   */
  override val isException: Boolean
    get() = this.innerOutcomeType is ExceptionOutcomeType

  /**
   * The exception that was thrown, if the compilation raised an unhandled exception.
   * Otherwise, this is always null.
   */
  override val exception: Exception?
    get() = (this.innerOutcomeType as? ExceptionOutcomeType)?.cause

  override fun toString() = this.innerOutcomeType.toString()

  private sealed interface InnerOutcomeType

  private object SuccessOutcomeType : InnerOutcomeType {
    override fun toString() = "success"
  }

  private object FailureOutcomeType : InnerOutcomeType {
    override fun toString() = "failure"
  }

  @JvmInline
  private value class ExceptionOutcomeType(val cause: Exception) : InnerOutcomeType {
    override fun toString() =
        "unhandled exception thrown (${cause.javaClass.simpleName}: ${cause.message})"
  }
}