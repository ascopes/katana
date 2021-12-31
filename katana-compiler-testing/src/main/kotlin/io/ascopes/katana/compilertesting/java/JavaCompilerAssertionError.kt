package io.ascopes.katana.compilertesting.java

import javax.tools.JavaFileObject


/**
 * Assertion error thrown to describe when a result has occurred that does not match the
 * expectation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 * @param message the error message.
 * @param files the file manager.
 * @param diagnostics any diagnostics that were reported.
 * @param logs any compiler logs that were generated.
 * @param expected the expected result.
 * @param actual the actual result.
 * @param cause the cause of the error, or `null` if not applicable.
 */
@Suppress("MemberVisibilityCanBePrivate")
class JavaCompilerAssertionError(
    message: String,
    val files: InMemoryFileManager,
    val diagnostics: List<DiagnosticWithTrace<out JavaFileObject>>?,
    val logs: String,
    val expected: Any? = null,
    val actual: Any? = null,
    cause: Throwable? = null
) : AssertionError(
    generateMessageFor(
        message,
        files,
        expected,
        actual,
        diagnostics,
        logs
    ),
    cause
)
