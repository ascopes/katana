package io.ascopes.katana.compilertesting.java

import io.ascopes.katana.compilertesting.BasicCompilationResult
import io.ascopes.katana.compilertesting.CompilationBuilder
import io.ascopes.katana.compilertesting.StackTraceProvider
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.Reader
import java.io.StringWriter
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.Locale
import javax.annotation.processing.Processor
import javax.lang.model.SourceVersion
import javax.tools.FileObject
import javax.tools.JavaCompiler
import javax.tools.JavaFileManager.Location
import javax.tools.JavaFileObject.Kind
import javax.tools.StandardLocation
import javax.tools.ToolProvider
import kotlin.io.path.absolutePathString
import kotlin.io.path.readBytes

/**
 * Support for compiling Java files located in a virtual in-memory filesystem.
 *
 * This supports Java 9 and newer. The compiler interface provided with JDK-8 and older is not
 * supported; however, you can still cross compile from JDK 9 or newer to JDK-8 or older.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class JavaCompilationBuilder
  : CompilationBuilder<JavaCompilation, JavaCompilationBuilder> {

  // Exposed for testing purposes only.
  internal val compiler: JavaCompiler
  internal val diagnosticListener: JavaDiagnosticListener
  internal val fileManager: JavaRamFileManager
  internal val options: MutableList<String>
  internal val modules: MutableSet<String>
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
    this.options = mutableListOf()
    this.modules = mutableSetOf()
    this.processors = mutableListOf()
  }

  /**
   * Invoke the compiler with the given inputs, and return the compilation result.
   *
   * @return the compilation result.
   */
  override fun compile(): JavaCompilation {
    val nonModuleCompilationUnits = this.fileManager
        .list(StandardLocation.SOURCE_PATH, "", setOf(Kind.SOURCE), true)
        .toList()

    val moduleCompilationUnits = this.fileManager
        .listLocationsForModules(StandardLocation.MODULE_SOURCE_PATH)
        .flatten()
        .flatMap { this.fileManager.list(it, "", setOf(Kind.SOURCE), true) }
        .toList()

    // Don't bother checking if both module and non-module sources exist. The file manager should
    // do this for us.
    val compilationUnits = nonModuleCompilationUnits + moduleCompilationUnits

    val stringWriter = StringWriter()

    val task = this.compiler.getTask(
        stringWriter,
        this.fileManager,
        this.diagnosticListener,
        this.options,
        null,
        compilationUnits
    )

    task.addModules(this.modules.sorted())
    task.setLocale(Locale.ROOT)
    task.setProcessors(this.processors)

    val outcome = try {
      BasicCompilationResult(task.call())
    } catch (ex: Exception) {
      BasicCompilationResult(ex)
    }

    return JavaCompilation(
        result = outcome,
        // Sort to ensure reproducible compilation calls.
        modules = this.modules,
        processors = this.processors,
        options = this.options,
        logs = stringWriter,
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
      HEADER_FLAG,
      this.fileManager
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
  fun includeModules(vararg moduleNames: String) = this.apply {
    this.modules += moduleNames
  }

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
  fun releaseVersion(version: Int) = this.options(RELEASE_FLAG, "$version")

  /**
   * Set the release version to use.
   *
   * You cannot set this as well as the source/target version, and you can only specify this once.
   *
   * @param version the release version.
   * @return this object for further call chaining.
   */
  fun releaseVersion(version: SourceVersion) = this.releaseVersion(sourceToInt(version))

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
  fun sourceVersion(version: Int) = this.options(SOURCE_FLAG, "$version")

  /**
   * Set the source version to use.
   *
   * You cannot set this as well as the release version, and you can only specify this once.
   *
   * @param version the source version.
   * @return this object for further call chaining.
   */
  fun sourceVersion(version: SourceVersion) = this.sourceVersion(sourceToInt(version))

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
  fun targetVersion(version: Int) = this.options(TARGET_FLAG, "$version")

  /**
   * Set the target version to use.
   *
   * You cannot set this as well as the release version, and you can only specify this once.
   *
   * @param version the target version.
   * @return this object for further call chaining.
   */
  fun targetVersion(version: SourceVersion) = this.targetVersion(sourceToInt(version))

  /**
   * Treat all warnings as errors.
   *
   * @return this object for further call chaining.
   */
  fun treatWarningsAsErrors() = this.options(WERROR_FLAG)

  /**
   * Step in the [JavaCompilationBuilder] that can create files for a location.
   * <p>
   * Once complete, call the [and] method to get a reference to the original builder.
   */
  inner class FileBuilder internal constructor(
      private val location: JavaRamLocation
  ) {

    /**
     *  Return the reference to the compilation builder. Same as calling [then].
     */
    fun and() = this@JavaCompilationBuilder

    /**
     * Return the reference to the compilation builder. Same as calling [and].
     */
    fun then() = this@JavaCompilationBuilder

    /**
     * Create a file with the given byte content.
     *
     * @param newFileName the name of the file.
     * @param bytes the byte content of the file.
     * @return this builder, for further call chaining.
     */
    fun create(newFileName: String, bytes: ByteArray) = this
        .doCreate(newFileName) { bytes }

    /**
     * Create a file with the given lines of content.
     *
     * @param newFileName the name of the file.
     * @param lines the lines of content to write.
     * @param lineSeparator the line separator to use, defaults to '\n'.
     * @param charset the charset to write as, defaults to 'UTF-8'.
     * @return this builder, for further call chaining.
     */
    @JvmOverloads
    fun create(
        newFileName: String,
        vararg lines: String,
        lineSeparator: String = "\n",
        charset: Charset = StandardCharsets.UTF_8,
    ) = this.doCreate(newFileName) {
      lines
          .joinToString(separator = lineSeparator)
          .toByteArray(charset = charset)
    }

    /**
     * Add a file from the classpath.
     *
     * @param classLoader the class loader to use, defaults to the classloader of this class.
     * @param classPathFile the class path file to add.
     * @param newFileName the name to give the file that will be created.
     * @throws FileNotFoundException if the file cannot be read.
     * @return this builder, for further call chaining.
     */
    @Throws(FileNotFoundException::class)
    fun copyFromClassPath(
        classLoader: ClassLoader,
        classPathFile: String,
        newFileName: String,
    ) = this
        .doCreate(newFileName) {
          classLoader
              .getResource(classPathFile)
              ?.readBytes()
              ?: throw FileNotFoundException(
                  "Could not read $classPathFile on class path for $classLoader"
              )
        }

    /**
     * Add a file from the current classpath.
     *
     * @param classPathFile the class path file to add.
     * @param newFileName the name to give the file that will be created.
     * @throws FileNotFoundException if the file cannot be read.
     * @return this builder, for further call chaining.
     */
    @Throws(FileNotFoundException::class)
    fun copyFromClassPath(
        classPathFile: String,
        newFileName: String,
    ) = this
        .copyFromClassPath(this::class.java.classLoader, classPathFile, newFileName)

    /**
     * Add a file from the host file system.
     *
     * @param filePath the path to the file on the file system to use.
     * @param newFileName the name to give the file that will be created.
     * @throws IOException if the file cannot be read.
     * @return this builder, for further call chaining.
     */
    @Throws(IOException::class)
    fun copyFrom(filePath: Path, newFileName: String) = this
        .doCreate(newFileName) { filePath.readBytes() }

    /**
     * Add a file from the given compiler file object.
     *
     * @param fileObject the file object to use.
     * @param newFileName the name to give the file that will be created.
     * @throws IOException if the file object cannot be read.
     * @return this builder, for further call chaining.
     */
    @Throws(IOException::class)
    fun copyFrom(fileObject: FileObject, newFileName: String) = this
        .doCreate(newFileName) { fileObject.openInputStream().use { it.readAllBytes() } }

    /**
     * Add a file from the given input stream.
     *
     * @param inputStream the input stream to read from.
     * @param newFileName the name to give to the file that will be created.
     * @throws IOException if the stream cannot be read.
     */
    @Throws(IOException::class)
    fun copyFrom(inputStream: InputStream, newFileName: String) = this
        .doCreate(newFileName) { inputStream.buffered().use { it.readAllBytes() } }

    /**
     * Add a file from the given reader.
     *
     * @param reader the reader to read from.
     * @param newFileName the name to give to the file that will be created.
     * @param charset the charset to encode the file as. Defaults to `UTF-8` if omitted.
     * @throws IOException if the stream cannot be read.
     */
    @JvmOverloads
    @Throws(IOException::class)
    fun copyFrom(
        reader: Reader,
        newFileName: String,
        charset: Charset = StandardCharsets.UTF_8
    ) = this
        .doCreate(newFileName) {
          val text = reader.use { it.readText() }
          return@doCreate text.toByteArray(charset = charset)
        }

    /**
     * Add a file by reading the contents from the given URL.
     *
     * @param url the URL to fetch the contents from.
     * @param newFileName the name to give to the file that will be created
     * @throws IOException if the URL contents could not be downloaded.
     * @return this builder, for further call chaining.
     */
    @Throws(IOException::class)
    fun copyFrom(url: URL, newFileName: String) = this
        .doCreate(newFileName) { url.readBytes() }

    private inline fun doCreate(newFileName: String, supplier: () -> ByteArray) = apply {
      val data = supplier()

      this@JavaCompilationBuilder
          .fileManager
          .createFile(location, newFileName, data)
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