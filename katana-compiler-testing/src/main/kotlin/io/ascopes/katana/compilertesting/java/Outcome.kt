package io.ascopes.katana.compilertesting.java

/**
 * Base marker interface for a compilation outcome.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
sealed interface Outcome

/**
 * Marker to indicate a successful compilation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
object Ok : Outcome

/**
 * Marker to indicate compilation failed in a non-exceptional way.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
object Failure : Outcome


/**
 * Marker to indicate compilation failed with an exception, unexpectedly.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
class FatalError(val reason: Throwable) : Outcome