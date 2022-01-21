package io.ascopes.katana.compilertesting

import java.io.IOException
import java.io.Writer
import mu.KotlinLogging


/**
 * Thread-safe writer that outputs each line that is written to the given logger,
 * and also caches any lines in-memory to be checked afterwards.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
class LoggingWriter : Writer(Any()) {
  private val logger = KotlinLogging.logger { }
  private val entireOutput = StringBuilder()
  private val currentLine = StringBuilder()

  @Volatile
  private var closed = false

  override fun close() {
    synchronized(lock) {
      if (closed) {
        return
      }

      if (currentLine.isNotEmpty()) {
        writeLine()
      }

      closed = true
    }
  }

  override fun flush() {
    // Do nothing.
  }

  override fun write(cbuf: CharArray, off: Int, len: Int) {
    synchronized(lock) {
      if (closed) {
        throw IOException("File writer is closed")
      }

      for (index in off until off + len) {
        val char = cbuf[index]

        if (char == '\n') {
          writeLine()
        } else {
          currentLine.append(char)
        }
      }
    }
  }

  override fun toString(): String {
    synchronized(lock) {
      if (!closed) {
        throw IllegalStateException(
            "Cannot read the contents of the output until the reader is closed"
        )
      }

      return entireOutput.toString()
    }
  }

  private fun writeLine() {
    val line = currentLine.toString()
    logger.info {
      // Don't output OS-specific newlines, as it will lead to weird logs on some platforms
      "output: " + line.replace("\r", "")
    }
    entireOutput.appendLine(line)
    currentLine.clear()
  }
}