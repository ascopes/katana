package io.ascopes.katana.compilertesting

/**
 * Base interface for the outcome of a compilation.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
interface Compilation<R : CompilationResult> {
  /**
   * The result type for the outcome of the compilation.
   *
   * This determines whether compilation succeeded, failed, or produced an unhandled exception.
   */
  val result: R
}