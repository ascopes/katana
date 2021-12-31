package io.ascopes.katana.compilertesting.java

/**
 * Base marker interface for a compilation outcome.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
sealed interface Outcome {
  val description: String
}

/**
 * Marker to indicate a successful compilation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
object Success : Outcome {
  override val description = "success"
}

/**
 * Marker to indicate compilation failed in a non-exceptional way.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
object Failure : Outcome {
  override val description = "failure"
}

/**
 * Marker to indicate compilation failed with an exception, unexpectedly.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
class FatalError(
    @Suppress("MemberVisibilityCanBePrivate") val reason: Throwable
) : Outcome {
  override val description: String
    get() = "${Companion.description} due to ${this.reason.javaClass.simpleName}"

  companion object {
    const val description: String = "fatal compiler error"
  }
}