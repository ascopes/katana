package io.ascopes.katana.compilertesting.java

import org.opentest4j.AssertionFailedError
import javax.tools.JavaFileObject


/**
 * Assertion error thrown to describe when a result has occurred that does not match the
 * expectation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@Suppress("MemberVisibilityCanBePrivate")
class JavaCompilerAssertionError : AssertionFailedError {
  private val fullMessage: String
  private val diagnostics: List<DiagnosticWithTrace<out JavaFileObject>>?
  private val logs: String

  constructor(
      message: String,
      files: InMemoryFileManager,
      diagnostics: List<DiagnosticWithTrace<out JavaFileObject>>?,
      logs: String,
      cause: Throwable? = null
  ) : super(message, cause) {
    this.fullMessage = Companion.generateMessageFor(super.message!!, files, diagnostics, logs)
    this.diagnostics = diagnostics
    this.logs = logs
  }

  constructor(
      message: String,
      files: InMemoryFileManager,
      diagnostics: List<DiagnosticWithTrace<out JavaFileObject>>?,
      logs: String,
      expected: Any?,
      actual: Any?,
      cause: Throwable? = null
  ) : super(message, expected, actual, cause) {
    this.fullMessage = Companion.generateMessageFor(super.message!!, files, diagnostics, logs)
    this.diagnostics = diagnostics
    this.logs = logs
  }

  // Doing this allows us to keep the massive error messages out of the Maven build logs, while
  // allowing Maven to display useful messages and letting IDEs handle producing full details.
  // This mirrors how JUnit and AssertJ handle assertion error reporting!
  override fun toString() = this.fullMessage

  companion object {
    private fun generateMessageFor(
        message: String,
        files: InMemoryFileManager,
        diagnostics: List<DiagnosticWithTrace<out JavaFileObject>>?,
        logs: String?
    ) = StringBuilder(message)
        .appendFileTree(files)
        .appendDiagnostics(diagnostics)
        .appendLogs(logs)
        .toString()

    private fun StringBuilder.appendDiagnostics(
        diagnostics: List<DiagnosticWithTrace<out JavaFileObject>>?
    ): StringBuilder {
      if (diagnostics == null || diagnostics.isEmpty()) {
        return this
      }

      this.appendLine()
      this.appendLine("Compilation produced the following diagnostics:")

      diagnostics.forEach { diagnostic ->
        val headline = if (diagnostic.code != null) {
          "${diagnostic.timestamp} - ${diagnostic.code}"
        } else {
          diagnostic.timestamp.toString()
        }
        val messageLines = diagnostic.toString().lines()
        val stackTraceLines = Companion.formatStackTraceLines(diagnostic.stacktrace)
        val maxLineLength = sequenceOf(listOf(headline), messageLines, stackTraceLines)
            .flatten()
            .maxOf { it.length }

        this.appendTopOfBox(headline, maxLineLength)
            .appendBoxLine("", maxLineLength)
            .appendBoxLines(messageLines, maxLineLength)
            .appendBoxSeparator("Reporting location", maxLineLength)
            .appendBoxLines(stackTraceLines, maxLineLength)
            .appendBottomOfBox(maxLineLength)
      }

      return this
    }

    private fun StringBuilder.appendLogs(logs: String?): StringBuilder {
      if (logs == null || logs.isBlank()) {
        return this
      }

      val lines = logs.lines()
      val maxLineLength = lines.maxOf { it.length }

      return this
          .appendLine()
          .appendTopOfBox("Compiler produced the following logs", maxLineLength)
          .appendBoxLines(lines, maxLineLength)
          .appendBottomOfBox(maxLineLength)
    }

    private fun StringBuilder.appendFileTree(fileManager: InMemoryFileManager): StringBuilder {
      val files = fileManager.listAllInMemoryFiles()
          .toList()

      val inputHeader = "Input files"
      val outputHeader = "Output files"
      val maxLineLength = sequenceOf(listOf(inputHeader, outputHeader), files.map { it.name })
          .flatten()
          .maxOf { it.length }

      return this
          .appendTopOfBox(inputHeader, maxLineLength)
          .appendBoxLines(
              files.filter { !it.location.isOutputLocation }.map { it.name },
              maxLineLength
          )
          .appendBoxSeparator(outputHeader, maxLineLength)
          .appendBoxLines(
              files.filter { it.location.isOutputLocation }.map { it.name },
              maxLineLength
          )
          .appendBottomOfBox(maxLineLength)
    }

    private fun StringBuilder.appendTopOfBox(content: String, maxLineLength: Int) = this
        .append("┏╸ ")
        .append("$content ╺".padEnd(maxLineLength, '━'))
        .appendLine("┓")

    private fun StringBuilder.appendBottomOfBox(maxLineLength: Int) = this
        .append("┗╸")
        .append("".padEnd(maxLineLength, '━'))
        .appendLine("━┛")

    private fun StringBuilder.appendBoxSeparator(content: String, maxLineLength: Int) = this
        .append("┠╴ ")
        .append("$content ╶".padEnd(maxLineLength, '─'))
        .appendLine("┨")

    private fun StringBuilder.appendBoxLine(content: String, maxLineLength: Int) = this
        .append("┃ ")
        .append(content.padEnd(maxLineLength, ' '))
        .appendLine(" ┃")

    private fun StringBuilder.appendBoxLines(
        lines: Iterable<String>,
        maxLineLength: Int
    ): StringBuilder {
      lines.forEach { this.appendBoxLine(it, maxLineLength) }
      return this
    }

    private fun formatStackTraceLines(stackTraceElement: Iterable<StackTraceElement>) =
        stackTraceElement
            .takeWhile { !it.className.startsWith("org.junit.platform.") }
            .map { " --> $it" }
            .toList()
  }
}
