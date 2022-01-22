package io.ascopes.katana.compilertesting.java

import io.ascopes.katana.compilertesting.core.BasicCompilationResult
import io.ascopes.katana.compilertesting.core.CompilationBuilder
import io.ascopes.katana.compilertesting.core.CompilationResult
import io.ascopes.katana.compilertesting.core.FileBuilder
import io.ascopes.katana.compilertesting.core.LoggingWriter
import io.ascopes.katana.compilertesting.core.StackTraceProvider
import java.io.File
import java.net.URLClassLoader
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
import kotlin.system.measureNanoTime
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import mu.KLogger
import mu.KotlinLogging

/**
 * Support for compiling Java files located in a virtual in-memory filesystem.
 *
 * This supports Java 9 and newer. The compiler interface provided with JDK-8 and older is not
 * supported; however, you can still cross compile from JDK 9 or newer to JDK-8 or older.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
@Suppress("MemberVisibilityCanBePrivate", "unused", "JoinDeclarationAndAssignment")
class JavaCompilationBuilder
  : CompilationBuilder<JavaCompilation, JavaCompilationBuilder> {

  // Exposed for testing purposes only.
  internal val classPath: MutableList<File>
  internal val compiler: JavaCompiler
  internal val diagnosticListener: JavaDiagnosticListener
  internal val fileManager: JavaRamFileManager
  internal val logger: KLogger
  internal val modules: MutableSet<String>
  internal val options: MutableList<String>
  internal var prependTestClassPath: Boolean
  internal val processors: MutableList<Processor>

  /**
   * @param compiler the Java compiler implementation to use.
   * @param diagnosticListener the diagnostic listener implementation to use.
   * @param fileManager the file manager implementation to use.
   */
  @Suppress("ConvertSecondaryConstructorToPrimary")
  internal constructor(
      compiler: JavaCompiler,
      diagnosticListener: JavaDiagnosticListener,
      fileManager: JavaRamFileManager
  ) {
    this.compiler = compiler
    this.diagnosticListener = diagnosticListener
    this.fileManager = fileManager

    classPath = mutableListOf()
    logger = KotlinLogging.logger { }
    modules = mutableSetOf()
    options = mutableListOf()
    prependTestClassPath = true
    processors = mutableListOf()
  }

  /**
   * Append the classpath with the given paths.
   *
   * These must be valid files on the default file system, or compilation will probably fail.
   *
   * @param paths the paths to add.
   * @return this object for further call chaining.
   */
  fun appendClassPath(vararg paths: String) = apply {
    paths
        .map { File(it) }
        .toList()
        .let { classPath.addAll(it) }
  }

  /**
   * Append the classpath with the contents of the given [URLClassLoader]s.
   *
   * These must be valid files on the default file system, or compilation will probably fail.
   *
   * @param classLoaders the class loaders to add the paths from.
   * @return this object for further call chaining.
   */
  fun appendClassPath(vararg classLoaders: URLClassLoader) = apply {
    classLoaders
        .flatMap { @Suppress("UsePropertyAccessSyntax") it.getURLs().asSequence() }
        .map { File(it.toURI()) }
        .toList()
        .let { classPath.addAll(it) }
  }

  /**
   * Invoke the compiler with the given inputs, and return the compilation result.
   *
   * @return the compilation result.
   */
  override fun compile(): JavaCompilation {
    // Set the classpath.
    val classPath = generateFinalClassPath().toList()
    fileManager.setClassPath(classPath)

    // Don't bother checking if both module and non-module sources exist. The file manager should
    // do this for us.
    val compilationUnits = collectCompilationUnits()

    val outputLogger = LoggingWriter()

    logger.debug {
      val initialFiles = fileManager.listAllInMemoryFiles().toList()

      StringBuilder("Compilation settings:")
          .appendLine()
          .appendLine(" * Classpath (${classPath.size} path(s)):")
          .apply { classPath.forEach { appendLine("    · ${it.toURI()}") } }
          .appendLine(" * Compilation units (${compilationUnits.size} file(s)):")
          .apply { compilationUnits.forEach { appendLine("    · ${it.toUri()}") } }
          .appendLine(" * Compiler: $compiler")
          .appendLine(" * Initial file system contents (${initialFiles.size} file(s)):")
          .apply { initialFiles.forEach { appendLine("    · ${it.toUri()}") } }
          .appendLine(" * Modules (${modules.size} item(s)):")
          .apply { modules.forEach { appendLine("    · $it") } }
          .appendLine(" * Processors (${processors.size} object(s)):")
          .apply { processors.forEach { appendLine("    · $it") } }
          .appendLine(" * Options (${options.size} item(s)):")
          .apply { options.forEach { appendLine("    · <$it>") } }
          .appendLine(" * Target module mode: ${fileManager.moduleMode}")
          .toString()
          .trimEnd()
    }

    val task = compiler.getTask(
        outputLogger,
        fileManager,
        diagnosticListener,
        options,
        null,  // Always null, we do not filter this by default.
        compilationUnits
    )

    task.addModules(modules.sorted())
    task.setLocale(Locale.ROOT)
    task.setProcessors(processors)

    val (result, timeTaken) = doCompile(task)

    outputLogger.close()
    val stderr = outputLogger.toString()

    logger.debug {
      val outcomeName = when {
        result.isSuccess -> "succeeded"
        result.isFailure -> "failed"
        else -> "aborted"
      }

      val finalFiles = fileManager.listAllInMemoryFiles().toList()

      StringBuilder("Compilation $outcomeName")
          .appendLine()
          .appendLine(" * Time taken: $timeTaken")
          .appendLine(" * Compiler logs: ${stderr.toByteArray().size} byte(s)")
          .appendLine(" * Diagnostic count: ${diagnosticListener.diagnostics.size}")
          .appendLine(" * Final file system contents (${finalFiles.size} file(s)):")
          .apply { finalFiles.forEach { appendLine("    · ${it.toUri()}") } }
          .toString()
          .trimEnd()
    }


    return JavaCompilation(
        result = result,
        // Sort to ensure reproducible compilation calls.
        modules = modules,
        processors = processors,
        options = options,
        logs = stderr,
        diagnostics = diagnosticListener.diagnostics,
        fileManager = fileManager
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
  fun files(location: Location, moduleName: String? = null): JavaFileBuilder {
    val ramLocation = if (moduleName == null) {
      fileManager.getInMemoryLocationFor(location)
    } else {
      fileManager.getInMemoryLocationFor(location, moduleName)
    }

    return JavaFileBuilder(ramLocation)
  }

  /**
   * Request that the compiler produces native headers for JNI interop.
   *
   * @return this object for further call chaining.
   */
  fun generateHeaders() = options(
      HEADER_FLAG,
      fileManager
          .getInMemoryLocationFor(StandardLocation.NATIVE_HEADER_OUTPUT)
          .path
          .absolutePathString()
  )

  /**
   * Add the following modules to the compilation to be considered.
   *
   * You probably don't need to use this...
   *
   * @param moduleNames the names of the modules to add.
   * @return this object for further call chaining.
   */
  fun includeModules(vararg moduleNames: String) = apply {
    modules += moduleNames
  }

  /**
   * Get a file builder for a multi-module compilation. This will not work if you
   * have already specified [sources] on this builder.
   *
   * @param moduleName the module to get the builder for.
   * @return the file builder.
   */
  fun multiModuleSources(moduleName: String) =
      files(StandardLocation.MODULE_SOURCE_PATH, moduleName)

  /**
   * Add the given options to the compiler.
   *
   * @param options the options to add.
   * @return this object for further call chaining.
   */
  fun options(vararg options: String) = apply { this.options += options }

  /**
   * Add the given processors to the compiler.
   *
   * @param processors the processors to add.
   * @return this object for further call chaining.
   */
  fun processors(vararg processors: Processor) = apply { this.processors += processors }

  /**
   * Set the release version to use.
   *
   * You cannot set this as well as the source/target version, and you can only specify this once.
   *
   * @param version the release version.
   * @return this object for further call chaining.
   */
  fun releaseVersion(version: Int) = options(RELEASE_FLAG, "$version")

  /**
   * Set the release version to use.
   *
   * You cannot set this as well as the source/target version, and you can only specify this once.
   *
   * @param version the release version.
   * @return this object for further call chaining.
   */
  fun releaseVersion(version: SourceVersion) = releaseVersion(sourceToInt(version))

  /**
   * Get a file builder for a single-module/no-module compilation. This will not work if you
   * have already specified [multiModuleSources] on this builder.
   *
   * @return the file builder.
   */
  fun sources() = files(StandardLocation.SOURCE_PATH)

  /**
   * Set the source version to use.
   *
   * You cannot set this as well as the release version, and you can only specify this once.
   *
   * @param version the source version.
   * @return this object for further call chaining.
   */
  fun sourceVersion(version: Int) = options(SOURCE_FLAG, "$version")

  /**
   * Set the source version to use.
   *
   * You cannot set this as well as the release version, and you can only specify this once.
   *
   * @param version the source version.
   * @return this object for further call chaining.
   */
  fun sourceVersion(version: SourceVersion) = sourceVersion(sourceToInt(version))

  /**
   * Set the source and target version to use.
   *
   * You cannot set this as well as the release version, nor the source and target version if you
   * specified those individually. Additionally, you can only specify this once.
   *
   * @param version the source and target version.
   * @return this object for further call chaining.
   */
  fun sourceAndTargetVersion(version: Int) = sourceVersion(version).targetVersion(version)

  /**
   * Set the source and target version to use.
   *
   * You cannot set this as well as the release version, nor the source and target version if you
   * specified those individually. Additionally, you can only specify this once.
   *
   * @param version the source and target version.
   * @return this object for further call chaining.
   */
  fun sourceAndTargetVersion(version: SourceVersion) = sourceVersion(version).targetVersion(version)

  /**
   * Set the target version to use.
   *
   * You cannot set this as well as the release version, and you can only specify this once.
   *
   * @param version the target version.
   * @return this object for further call chaining.
   */
  fun targetVersion(version: Int) = options(TARGET_FLAG, "$version")

  /**
   * Set the target version to use.
   *
   * You cannot set this as well as the release version, and you can only specify this once.
   *
   * @param version the target version.
   * @return this object for further call chaining.
   */
  fun targetVersion(version: SourceVersion) = targetVersion(sourceToInt(version))

  /**
   * Treat all warnings as errors.
   *
   * @return this object for further call chaining.
   */
  fun treatWarningsAsErrors() = options(WERROR_FLAG)

  /**
   * Enable or disable the inclusion of the current class path of the running JVM for the
   * compilation task when it runs.
   *
   * By default, this is enabled, but you can disable it or re-enable it elsewhere using this
   * method.
   *
   * The impact of disabling this is that any dependencies in the current JVM classpath will not be
   * detectable by the compiler when it runs. Generally this is not overly useful, but you may
   * have scenarios where you want to be able to control this.
   *
   * This does not impact anything you append to the classpath manually.
   *
   * @param useClassPath true to use the class path (default), or false to disable it.
   * @return this object for further call chaining.
   */
  @JvmOverloads
  fun useCurrentClassPath(useClassPath: Boolean = true) = apply {
    logger.debug {
      val value = if (useClassPath) "enabled" else "disabled"
      "Use of current JVM classpath is $value"
    }
    prependTestClassPath = useClassPath
  }

  /**
   * Step in the [JavaCompilationBuilder] that can create files for a location.
   * <p>
   * Once complete, call the [and] method to get a reference to the original builder.
   */
  inner class JavaFileBuilder internal constructor(
      private val location: JavaRamLocation
  ) : FileBuilder<JavaFileBuilder>() {

    /**
     *  Return the reference to the compilation builder. Same as calling [then].
     */
    fun and() = this@JavaCompilationBuilder

    /**
     * Return the reference to the compilation builder. Same as calling [and].
     */
    fun then() = this@JavaCompilationBuilder

    override fun doCreate(newFileName: String, contentProvider: FileContentProvider) = apply {
      val data = contentProvider()

      fileManager
          .createFile(location, newFileName, data)
    }
  }

  private fun generateFinalClassPath(): Iterable<File> {
    val classPath = LinkedHashSet<File>()

    if (prependTestClassPath) {
      val rawClassPath = System.getProperty("java.class.path", "")!!
      val rawSeparator = System.getProperty("path.separator", File.pathSeparator)!!
      val rawModulePath = System.getProperty("jdk.module.path", "")!!

      rawClassPath
          .split(Regex.fromLiteral(rawSeparator))
          .map { File(it) }
          .also { classPath.addAll(it) }

      rawModulePath
          .split(Regex.fromLiteral(rawSeparator))
          .map { File(it) }
          .also { classPath.addAll(it) }
    }

    classPath.addAll(this.classPath)

    return classPath
  }

  private fun collectCompilationUnits() = fileManager
      .run {
        sequence {
          yield(StandardLocation.SOURCE_PATH)
          yieldAll(listLocationsForModules(StandardLocation.MODULE_SOURCE_PATH).flatten())
        }
      }
      .flatMap { fileManager.list(it, "", setOf(Kind.SOURCE), true) }
      .toList()

  private fun doCompile(task: JavaCompiler.CompilationTask): CompilationData {
    val resultRef: BasicCompilationResult

    val timeTaken = measureNanoTime {
      resultRef = try {
        BasicCompilationResult(task.call())
      } catch (ex: Exception) {
        BasicCompilationResult(ex)
      }
    }.toDuration(DurationUnit.NANOSECONDS)

    return CompilationData(resultRef, timeTaken)
  }

  private data class CompilationData(
      val result: CompilationResult,
      val timeTaken: Duration
  )

  companion object {
    // Newer OpenJDK versions allow both --source and -source, but JDK-11 does not support --source.
    internal const val SOURCE_FLAG = "-source"

    // Newer OpenJDK versions allow both --target and -target, but JDK-11 does not support --target.
    internal const val TARGET_FLAG = "-target"

    // Release has two dashes, OpenJDK has started to adopt GNU-style flags now.
    internal const val RELEASE_FLAG = "--release"

    // Flag to treat warnings as errors.
    internal const val WERROR_FLAG = "-Werror"

    // Flag to specify header output locations
    internal const val HEADER_FLAG = "-h"

    /**
     * Get a virtual Java compiler for the system default Java compiler implementation.
     *
     * @return the compilation builder for testing compilation passes in-memory.
     */
    @JvmStatic
    fun javac() = compiler(ToolProvider.getSystemJavaCompiler())

    /**
     * Get a virtual compiler for the given Java compiler implementation.
     *
     * @param compiler the actual Java compiler to use.
     * @return the compilation builder for testing compilation passes in-memory.
     */
    @JvmStatic
    fun compiler(compiler: JavaCompiler): JavaCompilationBuilder {
      val stackTraceProvider = StackTraceProvider.threadStackTraceProvider
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