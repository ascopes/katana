package io.ascopes.katana.compilertesting.java

import io.ascopes.katana.compilertesting.java.JavaCompilationResult.Failure
import io.ascopes.katana.compilertesting.java.JavaCompilationResult.FatalError
import io.ascopes.katana.compilertesting.java.JavaCompilationResult.Success
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.util.Locale
import javax.annotation.processing.Processor
import javax.lang.model.SourceVersion
import javax.tools.JavaCompiler
import javax.tools.JavaFileManager.Location
import javax.tools.JavaFileObject.Kind
import javax.tools.StandardLocation
import javax.tools.ToolProvider
import kotlin.io.path.absolutePathString

/**
 * Support for compiling files located in a virtual in-memory filesystem.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 * @param compiler the Java Compiler implementation to use.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class JavaCompilationBuilder internal constructor(
    // Exposed for testing purposes only.
    internal val compiler: JavaCompiler,
    internal val diagnosticListener: JavaDiagnosticListener,
    internal val fileManager: JavaRamFileManager,
) {
  // Exposed for testing purposes only.
  internal val options = mutableListOf<String>()
  internal val modules = mutableListOf<String>()
  internal val processors = mutableListOf<Processor>()

  /**
   * Invoke the compiler with the given inputs, and return the compilation result.
   *
   * @return the compilation result.
   */
  fun compile(): JavaCompilationResult {
    val nonModuleCompilationUnits = this.fileManager
        .list(StandardLocation.SOURCE_PATH, "", setOf(Kind.SOURCE), true)
        .toList()

    val moduleCompilationUnits = this.fileManager
        .listLocationsForModules(StandardLocation.MODULE_SOURCE_PATH)
        .flatten()
        .flatMap { this.fileManager.list(it, "", setOf(Kind.SOURCE), true) }
        .toList()

    val compilationUnits = if (nonModuleCompilationUnits.isEmpty()) {
      moduleCompilationUnits.isNotEmpty()
          || throw IllegalStateException("No sources or module sources were provided to compile")
      moduleCompilationUnits
    } else if (moduleCompilationUnits.isNotEmpty()) {
      throw IllegalStateException("Both sources and modular sources were provided")
    } else {
      nonModuleCompilationUnits
    }

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
      if (task.call()) Success else Failure
    } catch (ex: Exception) {
      FatalError(ex)
    }

    return JavaCompilationResult(
        outcome = outcome,
        modules = this.modules,
        processors = this.processors,
        options = this.options,
        logs = stringWriter.toString(),
        diagnostics = this.diagnosticListener.diagnostics,
        fileManager = this.fileManager
    )
  }

  /**
   * Get a file builder for the given location and optional module name.
   *
   * This will fail if the location is not an in-memory location, which only covers
   * [StandardLocation.SOURCE_PATH], [StandardLocation.SOURCE_OUTPUT],
   * [StandardLocation.MODULE_SOURCE_PATH], [StandardLocation.NATIVE_HEADER_OUTPUT],
   * and [StandardLocation.CLASS_OUTPUT].
   *
   * If you write any files to [StandardLocation.MODULE_SOURCE_PATH], it will make the compilation
   * invalid for anything in [StandardLocation.SOURCE_PATH], just as is the case with `javac`
   * itself.
   *
   * @param location the location.
   * @param moduleName the module name, or null if not applicable.
   * @return the file builder.
   */
  @JvmOverloads
  fun files(location: Location, moduleName: String? = null): FileBuilder {
    val ramLocation = if (moduleName == null) {
      this.fileManager.getInMemoryLocationFor(location)
    } else {
      this.fileManager.getInMemoryLocationFor(location, moduleName)
    }

    return FileBuilder(ramLocation)
  }

  /**
   * Request that the compiler produces native headers for JNI interop.
   *
   * @return this object for further call chaining.
   */
  fun generateHeaders() = this.options(
      Companion.HEADER_FLAG,
      this.fileManager
          .getInMemoryLocationFor(StandardLocation.NATIVE_HEADER_OUTPUT)
          .path
          .absolutePathString()
  )

  /**
   * Get a file builder for a multi-module compilation. This will not work if you
   * have already specified [sources] on this builder.
   *
   * @param moduleName the module to get the builder for.
   * @return the file builder.
   */
  fun multiModuleSources(moduleName: String) =
      this.files(StandardLocation.MODULE_SOURCE_PATH, moduleName)

  /**
   * Add the given options to the compiler.
   *
   * @param options the options to add.
   * @return this object for further call chaining.
   */
  fun options(vararg options: String) = this.apply { this.options += options }

  /**
   * Add the given processors to the compiler.
   *
   * @param processors the processors to add.
   * @return this object for further call chaining.
   */
  fun processors(vararg processors: Processor) = this.apply { this.processors += processors }

  /**
   * Set the release version to use.
   *
   * You cannot set this as well as the source/target version, and you can only specify this once.
   *
   * @param version the release version.
   * @return this object for further call chaining.
   */
  fun releaseVersion(version: Int) = this.options(Companion.RELEASE_FLAG, "$version")

  /**
   * Set the release version to use.
   *
   * You cannot set this as well as the source/target version, and you can only specify this once.
   *
   * @param version the release version.
   * @return this object for further call chaining.
   */
  fun releaseVersion(version: SourceVersion) = this.releaseVersion(Companion.sourceToInt(version))

  /**
   * Get a file builder for a single-module/no-module compilation. This will not work if you
   * have already specified [multiModuleSources] on this builder.
   *
   * @return the file builder.
   */
  fun sources() = this.files(StandardLocation.SOURCE_PATH)

  /**
   * Set the source version to use.
   *
   * You cannot set this as well as the release version, and you can only specify this once.
   *
   * @param version the source version.
   * @return this object for further call chaining.
   */
  fun sourceVersion(version: Int) = this.options(Companion.SOURCE_FLAG, "$version")

  /**
   * Set the source version to use.
   *
   * You cannot set this as well as the release version, and you can only specify this once.
   *
   * @param version the source version.
   * @return this object for further call chaining.
   */
  fun sourceVersion(version: SourceVersion) = this.sourceVersion(Companion.sourceToInt(version))

  /**
   * Set the source and target version to use.
   *
   * You cannot set this as well as the release version, nor the source and target version if you
   * specified those individually. Additionally, you can only specify this once.
   *
   * @param version the source and target version.
   * @return this object for further call chaining.
   */
  fun sourceAndTargetVersion(version: Int) = this
      .sourceVersion(version)
      .targetVersion(version)

  /**
   * Set the source and target version to use.
   *
   * You cannot set this as well as the release version, nor the source and target version if you
   * specified those individually. Additionally, you can only specify this once.
   *
   * @param version the source and target version.
   * @return this object for further call chaining.
   */
  fun sourceAndTargetVersion(version: SourceVersion) = this
      .sourceVersion(version)
      .targetVersion(version)

  /**
   * Set the target version to use.
   *
   * You cannot set this as well as the release version, and you can only specify this once.
   *
   * @param version the target version.
   * @return this object for further call chaining.
   */
  fun targetVersion(version: Int) = this.options(Companion.TARGET_FLAG, "$version")

  /**
   * Set the target version to use.
   *
   * You cannot set this as well as the release version, and you can only specify this once.
   *
   * @param version the target version.
   * @return this object for further call chaining.
   */
  fun targetVersion(version: SourceVersion) = this.targetVersion(Companion.sourceToInt(version))

  /**
   * Treat all warnings as errors.
   *
   * @return this object for further call chaining.
   */
  fun treatWarningsAsErrors() = this.options(Companion.WERROR_FLAG)

  /**
   * Step in the [JavaCompilationBuilder] that can create files for a location.
   * <p>
   * Once complete, call the [and] method to get a reference to the original builder.
   */
  inner class FileBuilder internal constructor(private val location: JavaRamLocation) {
    /**
     * Finish defining files in this location and get a reference to the compilation builder again.
     *
     * @return the [JavaCompilationBuilder] to chain further calls onto.
     */
    fun and() = this@JavaCompilationBuilder


    /**
     * Create a file with the given byte content.
     *
     * @param fileName the name of the file.
     * @param bytes the byte content of the file.
     * @return this builder, for further call chaining.
     */
    fun createFile(fileName: String, bytes: ByteArray) = this.apply {
      this@JavaCompilationBuilder
          .fileManager
          .createFile(location, fileName, bytes)
    }

    /**
     * Create a file with the given lines of content.
     *
     * @param fileName the name of the file.
     * @param lines the lines of content to write.
     * @return this builder, for further call chaining.
     */
    fun createFile(fileName: String, vararg lines: String) = this.apply {
      this@JavaCompilationBuilder
          .fileManager
          .createFile(
              location,
              fileName,
              lines.joinToString("\n").toByteArray()
          )
    }
  }

  companion object {
    // Newer OpenJDK versions allow both --source and -source, but JDK-11 does not support --source.
    internal const val SOURCE_FLAG = "-source"
    // Newer OpenJDK versions allow both --target and -target, but JDK-11 does not support --target.
    internal const val TARGET_FLAG = "-target"
    // Release has two dashes, OpenJDK has started to adopt GNU-style flags now.
    internal const val RELEASE_FLAG = "--release"
    // Flag to treat warnings as errors.
    internal  const val WERROR_FLAG = "-Werror"
    // Flag to specify header output locations
    internal const val HEADER_FLAG = "-h"

    /**
     * Get a virtual Java compiler for the system default Java compiler implementation.
     *
     * @return the compilation builder for testing compilation passes in-memory.
     */
    @JvmStatic
    fun javac() = this.compiler(ToolProvider.getSystemJavaCompiler())

    /**
     * Get a virtual compiler for the given Java compiler implementation.
     *
     * @param compiler the actual Java compiler to use.
     * @return the compilation builder for testing compilation passes in-memory.
     */
    @JvmStatic
    fun compiler(compiler: JavaCompiler): JavaCompilationBuilder {
      val stackTraceProvider = JavaStackTraceProvider.threadStackTraceProvider
      val diagnosticListener = JavaDiagnosticListener(stackTraceProvider)

      val standardFileManager = compiler.getStandardFileManager(
          diagnosticListener,
          Locale.ROOT,
          StandardCharsets.UTF_8
      )

      val fileManager = JavaRamFileManager(standardFileManager)

      return JavaCompilationBuilder(compiler, diagnosticListener, fileManager)
    }

    private fun sourceToInt(sourceVersion: SourceVersion) = sourceVersion
        .name
        .substringAfterLast('_')
        .toInt()
  }
}