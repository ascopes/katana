@file:JvmName("CompilerAssert")

/**
 * Assertion statements.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
package io.ascopes.katana.compilertesting.assertions

import io.ascopes.katana.compilertesting.compilation.JavaCompilation

/**
 * Start an assertion on a given [JavaCompilation].
 *
 * @param compilation the compilation.
 * @return the assertion builder for the compilation.
 */
fun assertThat(compilation: JavaCompilation) = JavaCompilationAssertions(compilation)