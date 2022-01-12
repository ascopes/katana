package io.ascopes.katana.compilertesting.compilation

/**
 * Base interface for anything that generates a compilation.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
interface CompilationBuilder<out C: Compilation<*>> {
  /**
   * Invoke the compiler and return the compilation result.
   *
   * @return the compilation.
   */
  fun compile(): C
}