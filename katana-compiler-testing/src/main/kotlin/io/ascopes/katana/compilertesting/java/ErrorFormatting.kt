package io.ascopes.katana.compilertesting.java

import javax.tools.JavaFileObject


/**
 * Generate an assertion error message for the given parameters.
 *
 * Providing both `expected` and `actual` as null will prevent that component being shown in the
 * message. Likewise, passing `diagnostics` and `logs` as null will prevent those from being
 * logged in the message.
 *
 * @param message the message to start the error with.
 * @param files the file manager.
 * @param expected the expected result.
 * @param actual the actual result.
 * @param diagnostics the diagnostics that were logged.
 * @param logs the logs from the compiler.
 * @return the formatted message.
 */
internal fun generateMessageFor(
    message: String,
    files: InMemoryFileManager,
    expected: Any?,
    actual: Any?,
    diagnostics: List<DiagnosticWithTrace<out JavaFileObject>>?,
    logs: String?
) = StringBuilder(message)
    .appendLine()
    .appendLine()
    .appendExpectedActual(expected, actual)
    .appendVirtualFileTree(files)
    .appendDiagnostics(diagnostics)
    .appendLogs(logs)
    .toString()

private fun StringBuilder.appendExpectedActual(expected: Any?, actual: Any?): StringBuilder {
  if (expected == null && actual == null) {
    return this
  }

  return this
      .appendLine()
      .appendLine("\tExpected :: <${expected}>")
      .appendLine("\tActual   :: <${actual}>")
}

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
    val stackTraceLines = formatStackTraceLines(diagnostic.stacktrace)
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

private fun StringBuilder.appendVirtualFileTree(fileManager: InMemoryFileManager): StringBuilder {
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