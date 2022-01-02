package io.ascopes.katana.compilertesting.java

import org.opentest4j.AssertionFailedError
import javax.annotation.processing.Processor
import javax.tools.JavaFileObject


/**
 * Assertion error thrown to describe when a result has occurred that does not match the
 * expectation.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
@Suppress("MemberVisibilityCanBePrivate")
internal class InMemoryCompilationAssertionError : AssertionFailedError {
  private val fullMessage: String
  private val compilationResult: InMemoryCompilationResult

  internal constructor(
      message: String,
      compilationResult: InMemoryCompilationResult,
      cause: Throwable? = null
  ) : super(message, cause) {
    this.fullMessage = Companion.generateMessageFor(
        super.message!!,
        compilationResult
    )
    this.compilationResult = compilationResult
  }

  internal constructor(
      message: String,
      compilationResult: InMemoryCompilationResult,
      expected: Any?,
      actual: Any?,
      cause: Throwable? = null
  ) : super(message, expected, actual, cause) {
    this.fullMessage = Companion.generateMessageFor(super.message!!, compilationResult)
    this.compilationResult = compilationResult
  }

  // Doing this allows us to keep the massive error messages out of the Maven build logs, while
  // allowing Maven to display useful messages and letting IDEs handle producing full details.
  // This mirrors how JUnit and AssertJ handle assertion error reporting!
  override fun toString() = this.fullMessage

  companion object {
    private fun generateMessageFor(
        message: String,
        compilationResult: InMemoryCompilationResult
    ) = StringBuilder(message)
        .appendOptions(compilationResult.options)
        .appendModules(compilationResult.modules)
        .appendProcessors(compilationResult.processors)
        .appendFileTree(compilationResult.fileManager)
        .appendDiagnostics(compilationResult.diagnostics)
        .appendLogs(compilationResult.logs)
        .toString()

    private fun StringBuilder.appendOptions(options: List<String>) = this
        .appendSimpleBox("Options", options)

    private fun StringBuilder.appendModules(modules: List<String>) = this
        .appendSimpleBox("Modules", modules)

    private fun StringBuilder.appendProcessors(processors: List<Processor>) = this
        .appendSimpleBox("Annotation Processors", processors.map { it::class.java.canonicalName })

    private fun StringBuilder.appendDiagnostics(
        diagnostics: List<InMemoryDiagnostic<out JavaFileObject>>?
    ): StringBuilder {
      if (diagnostics == null || diagnostics.isEmpty()) {
        return this
      }

      this.appendLine()
      this.appendLine("Compilation produced the following diagnostics:")

      diagnostics.forEach { diagnostic ->
        val title = if (diagnostic.code != null) {
          "${diagnostic.timestamp} - ${diagnostic.code}"
        } else {
          diagnostic.timestamp.toString()
        }
        val locationTitle = "Reporting location"
        val messageLines = diagnostic.toString().lines()
        val stackTraceLines = Companion.formatStackTraceLines(diagnostic.stacktrace)
        val maxLineLength = sequenceOf(listOf(title, locationTitle), messageLines, stackTraceLines)
            .flatten()
            .maxOf { it.length } + 5

        this.appendTopOfBox(title, maxLineLength)
            .appendBoxLine("", maxLineLength)
            .appendBoxLines(messageLines, maxLineLength)
            .appendBoxSeparator(locationTitle, maxLineLength)
            .appendBoxLines(stackTraceLines, maxLineLength)
            .appendBottomOfBox(maxLineLength)
      }

      return this
    }

    private fun StringBuilder.appendLogs(logs: String?) = if (logs != null && logs.isNotBlank()) {
      this.appendSimpleBox("Compiler logs", logs.lines())
    } else {
      this
    }

    private fun StringBuilder.appendFileTree(fileManager: InMemoryFileManager): StringBuilder {
      val files = fileManager.listAllInMemoryFiles()
          .toList()
          .sorted()
          .map { it.toString() }

      return this.appendSimpleBox("File system state", files)
    }

    private fun StringBuilder.appendSimpleBox(title: String, lines: List<String>): StringBuilder {

      if (lines.isEmpty()) {
        return this
      }

      val maxLineLength = sequenceOf(listOf(title), lines)
          .flatten()
          .maxOf { it.length } + 5

      return this
          .appendLine()
          .appendTopOfBox(title, maxLineLength)
          .appendBoxLines(lines, maxLineLength)
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
