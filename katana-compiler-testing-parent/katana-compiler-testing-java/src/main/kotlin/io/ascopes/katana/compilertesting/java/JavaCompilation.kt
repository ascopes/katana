package io.ascopes.katana.compilertesting.java

import io.ascopes.katana.compilertesting.core.Compilation
import io.ascopes.katana.compilertesting.core.CompilationResult
import javax.annotation.processing.Processor
import javax.tools.JavaFileObject


/**
 * Results for an in-memory compilation pass.
 *
 * @param result the outcome of the compilation.
 * @param modules the modules passed to the compiler.
 * @param processors the annotation processors passed to the compiler.
 * @param options the options passed to the compiler.
 * @param logs the standard output for the compiler, as a string.
 * @param diagnostics the diagnostics that the compiler output, along with call location details.
 * @param fileManager the file manager that was used.
 * @author Ashley Scopes
 * @since 0.1.0
 */
data class JavaCompilation internal constructor(
    override val result: CompilationResult,
    val modules: Set<String>,
    val processors: List<Processor>,
    val options: List<String>,
    val logs: String,
    val diagnostics: List<JavaRamDiagnostic<out JavaFileObject>>,
    val fileManager: JavaRamFileManager,
) : Compilation<CompilationResult>