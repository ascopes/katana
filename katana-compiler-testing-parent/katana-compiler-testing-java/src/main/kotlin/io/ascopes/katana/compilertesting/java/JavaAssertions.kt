package io.ascopes.katana.compilertesting.java


/**
 * Provides the `assertThatJavaCompilation` method to produce a compilation assertion fluently.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
object JavaAssertions {
  /**
   * Produce a Java Compilation assertion for the given Java compilation.
   */
  @JvmStatic
  fun assertThatJavaCompilation(compilation: JavaCompilation) = JavaCompilationAssertions(compilation)
}