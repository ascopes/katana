package io.ascopes.katana.compilertesting

/**
 * Base class for anything that generates a compilation.
 *
 * @param C the compilation result type.
 * @author Ashley Scopes
 * @since 0.1.0
 */
abstract class CompilationBuilder<out C, B>
  : PolymorphicTypeSafeBuilder<B>()
    where C : Compilation<*>, B : CompilationBuilder<C, B> {

  /**
   * Invoke the compiler and return the compilation result.
   *
   * @return the compilation.
   */
  abstract fun compile(): C
}