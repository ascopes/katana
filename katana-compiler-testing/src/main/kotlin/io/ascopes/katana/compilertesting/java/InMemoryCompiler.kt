package io.ascopes.katana.compilertesting.java

import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.nio.file.StandardOpenOption
import java.util.Locale
import javax.annotation.processing.Processor
import javax.tools.JavaCompiler
import javax.tools.JavaFileObject.Kind
import javax.tools.StandardLocation
import javax.tools.StandardLocation.SOURCE_PATH
import javax.tools.ToolProvider
import kotlin.io.path.writeBytes
import kotlin.io.path.writeLines

/**
 * Support for compiling files located in a virtual in-memory filesystem.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 * @param compiler the Java Compiler implementation to use.
 */
class InMemoryCompiler(private val compiler: JavaCompiler) {
  private val diagnosticListener = InMemoryDiagnosticListener()

  private val fileManager = InMemoryFileManager.create(
      this.compiler,
      this.diagnosticListener,
      Locale.ROOT,
      StandardCharsets.UTF_8
  )

  private val options = mutableListOf<String>()
  private val modules = mutableListOf<String>()
  private val processors = mutableListOf<Processor>()

  /**
   * Set the source version to use.
   *
   * @param version the source version.
   * @return this object.
   */
  @Suppress("MemberVisibilityCanBePrivate")
  fun sourceVersion(version: Int) = this.options("--source", version.toString())

  /**
   * Set the target version to use.
   *
   * @param version the target version.
   * @return this object.
   */
  @Suppress("MemberVisibilityCanBePrivate")
  fun targetVersion(version: Int) = this.options("--target", version.toString())

  /**
   * Set the source and target version to use.
   *
   * @param version the source and target version.
   * @return this object.
   */
  fun sourceAndTargetVersion(version: Int) = this.sourceVersion(version).targetVersion(version)

  /**
   * Add the given options to the compiler.
   *
   * @param options the options to add.
   * @return this object.
   */
  fun options(vararg options: String) = this.apply { this.options += options }

  /**
   * Add the given modules to the compiler.
   *
   * @param modules the modules to add.
   * @return this object.
   */
  fun modules(vararg modules: String) = this.apply { this.modules += modules }

  /**
   * Add the given processors to the compiler.
   *
   * @param processors the processors to add.
   * @return this object.
   */
  fun processors(vararg processors: Processor) = this.apply { this.processors += processors }

  /**
   * Create a file in the source path from the given lines of text.
   *
   * @param name the file name.
   * @param content the lines of text to add.
   * @return this object.
   */
  fun file(name: String, vararg content: CharSequence) = this.file(SOURCE_PATH, name, *content)

  /**
   * Create a file in the source path from the given byte content
   *
   * @param name the file name.
   * @param content the byte content to add.
   * @return this object.
   */
  fun file(name: String, content: ByteArray) = this.file(SOURCE_PATH, name, content)

  /**
   * Create a file in the given location from the given lines of text.
   *
   * @param location the standard location to write the file to.
   * @param name the file name.
   * @param content the lines of text to add.
   * @return this object.
   */
  @Suppress("MemberVisibilityCanBePrivate")
  fun file(location: StandardLocation, name: String, vararg content: CharSequence) = this.apply {
    this.fileManager
        .createFile(location, name)
        .writeLines(content.asSequence())
  }


  /**
   * Create a file in the given location from the given byte content
   *
   * @param location the standard location to write the file to.
   * @param name the file name.
   * @param content the byte content to add.
   * @return this object.
   */
  @Suppress("MemberVisibilityCanBePrivate")
  fun file(location: StandardLocation, name: String, content: ByteArray) = this.apply {
    this.fileManager
        .createFile(location, name)
        .writeBytes(content, StandardOpenOption.WRITE)
  }

  /**
   * Invoke the compiler with the given inputs, and return the compilation result.
   *
   * @return the compilation result.
   */
  fun compile(): InMemoryCompilationResult {
    val compilationUnits = this.fileManager.list(
        SOURCE_PATH,
        "",
        setOf(Kind.SOURCE),
        true
    )

    val stringWriter = StringWriter()

    val task = this.compiler.getTask(
        stringWriter,
        this.fileManager,
        this.diagnosticListener,
        this.options,
        null,
        compilationUnits
    )

    task.addModules(this.modules)
    task.setLocale(Locale.ROOT)
    task.setProcessors(this.processors)

    val outcome = try {
      if (task.call()) Ok else Failure
    } catch (ex: Exception) {
      FatalError(ex)
    }

    return InMemoryCompilationResult(
        outcome = outcome,
        logs = stringWriter.toString(),
        diagnostics = this.diagnosticListener.diagnostics,
        fileManager = this.fileManager
    )
  }

  private fun apply(operation: () -> Unit): InMemoryCompiler {
    operation()
    return this
  }

  companion object {
    /**
     * Get a virtual Java compiler with a backing virtual file system behind it.
     */
    @JvmStatic
    fun javac() = InMemoryCompiler(ToolProvider.getSystemJavaCompiler())
  }
}