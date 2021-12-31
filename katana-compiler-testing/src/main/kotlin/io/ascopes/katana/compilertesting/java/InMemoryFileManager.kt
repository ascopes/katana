package io.ascopes.katana.compilertesting.java

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import java.lang.ref.Cleaner
import java.nio.charset.Charset
import java.nio.file.FileSystem
import java.nio.file.FileVisitOption
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.Locale
import java.util.UUID
import javax.tools.DiagnosticListener
import javax.tools.FileObject
import javax.tools.ForwardingJavaFileManager
import javax.tools.JavaCompiler
import javax.tools.JavaFileManager
import javax.tools.JavaFileManager.Location
import javax.tools.JavaFileObject
import javax.tools.JavaFileObject.Kind
import javax.tools.StandardJavaFileManager
import javax.tools.StandardLocation
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isRegularFile


/**
 * Java file manager that decides whether to delegate to a given [StandardJavaFileManager]
 * based on the locations of the paths it is attempting to handle.
 *
 * For paths such as input sources, output sources, and output classes, this will delegate
 * IO operations to an in-memory file system hosted by `commons-vfs2`, and will
 * purge those locations when this object is garbage collected.
 *
 * This makes this very useful in tests where we need to present a full file system
 * to the compiler API.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 * @param standardFileManager the compiler-supplied standard file manager to delegate most calls to.
 */
class InMemoryFileManager(
    standardFileManager: StandardJavaFileManager
) : ForwardingJavaFileManager<JavaFileManager>(standardFileManager) {
  private val fs: FileSystem
  private val rootPath: Path

  init {
    val fsName = UUID.randomUUID().toString()
    this.fs = Jimfs.newFileSystem(fsName, Configuration.unix())
    this.rootPath = this.fs.getPath("/")

    // Create the root directories ready.
    Companion.virtualLocations.forEach { this.rootPath.resolve(it.name).createDirectory() }

    // On garbage collection, destroy any created files within the temporary root we created.
    Companion.cleaner.register(this, VfsRootHandle(this.fs))
  }

  /**
   * Create a file and return the path to it.
   *
   * @param location the location of the file.
   * @param fileName the name of the file.
   * @return the path of the file, which can be used to open a writer quickly.
   */
  fun createFile(location: Location, fileName: String): Path {
    val path = this.fileToPath(location, "", fileName)
    this.createNewFileWithDirectories(path)
    return path
  }

  /**
   * Get a reference to an in-memory file, if it exists.
   *
   * If it does not exist, return null.
   *
   * @param location the location of the file.
   * @param fileName the name of the file.
   * @return the file object, if it exists, otherwise `null`.
   */
  fun getInMemoryFile(location: Location, fileName: String): InMemoryFileObject? {
    if (location !in Companion.virtualLocations) {
      throw UnsupportedOperationException("Can get existing files from in-memory file system")
    }

    val path = this.fileToPath(location, "", fileName)
    val obj = InMemoryFileObject(location, path.toUri())
    return if (obj.exists()) obj else null
  }

  /**
   * Get an iterable across all in-memory files.
   *
   * @return the iterable sequence.
   */
  fun listAllInMemoryFiles(): Iterable<InMemoryFileObject> {
    return Companion
        .virtualLocations
        .flatMap { this.list(it, "", setOf(Kind.OTHER), true) }
        .map { it as InMemoryFileObject }
  }

  /**
   * Get a file for use as an input.
   *
   * @param location the location of the file.
   * @param packageName the package name of the file.
   * @param relativeName the relative name of the file.
   * @return a file object, or null if the file does not exist.
   */
  override fun getFileForInput(
      location: Location,
      packageName: String,
      relativeName: String
  ): FileObject? {
    if (location !in Companion.virtualLocations) {
      return super.getFileForInput(location, packageName, relativeName)
    }

    val path = this.fileToPath(location, packageName, relativeName)
    val obj = InMemoryFileObject(location, path.toUri())
    return if (obj.exists()) obj else null
  }

  /**
   * Get a source file for use as an input.
   *
   * @param location the location of the file.
   * @param className the qualified class name.
   * @param kind the file kind.
   * @return a file object, or null if the file does not exist.
   */
  override fun getJavaFileForInput(
      location: Location,
      className: String,
      kind: Kind
  ): JavaFileObject? {
    if (location !in Companion.virtualLocations) {
      return super.getJavaFileForInput(location, className, kind)
    }

    val path = this.sourceToPath(location, className, kind)
    val obj = InMemoryFileObject(location, path.toUri())
    return if (obj.exists()) obj else null
  }

  /**
   * Get a file for use as an output.
   *
   * @param location the location of the file.
   * @param packageName the package name of the file.
   * @param relativeName the relative name of the file.
   * @param sibling a nullable sibling to the file.
   * @return a file object.
   */
  override fun getFileForOutput(
      location: Location,
      packageName: String,
      relativeName: String,
      sibling: FileObject?
  ): FileObject {
    if (location !in Companion.virtualLocations) {
      return super.getFileForOutput(location, packageName, relativeName, sibling)!!
    }

    val path = this.fileToPath(location, packageName, relativeName)
    this.createNewFileWithDirectories(path)
    return InMemoryFileObject(location, path.toUri())
  }


  /**
   * Get a source file for use as an outpuit.
   *
   * @param location the location of the file.
   * @param className the qualified class name.
   * @param kind the file kind.
   * @param sibling a nullable sibling to the file.
   * @return a file object.
   */
  override fun getJavaFileForOutput(
      location: Location,
      className: String,
      kind: Kind,
      sibling: FileObject?
  ): JavaFileObject {
    if (location !in Companion.virtualLocations) {
      return super.getJavaFileForOutput(location, className, kind, sibling)!!
    }

    val path = this.sourceToPath(location, className, kind)
    this.createNewFileWithDirectories(path)
    return InMemoryFileObject(location, path.toUri())
  }

  /**
   * List the files in the given location and package, filtering by their kinds.
   *
   * @param location the location to enter.
   * @param packageName the package name to enter.
   * @param kinds the kinds of file to return.
   * @param recurse true to recursively list files in a top-down order, false to only list the
   *    given location and package name's files directly.
   * @return an iterable across the files that were found.
   */
  override fun list(
      location: Location,
      packageName: String,
      kinds: Set<Kind>,
      recurse: Boolean
  ): Iterable<JavaFileObject> {
    if (location !in Companion.virtualLocations) {
      return super.list(location, packageName, kinds, recurse)
    }

    val basePath = this.fileToPath(location, packageName, ".")
    val visitOpts = emptySet<FileVisitOption>()
    val maxDepth = if (recurse) Int.MAX_VALUE else 1
    val filesFound = mutableListOf<InMemoryFileObject>()

    val visitor = object : SimpleFileVisitor<Path>() {
      override fun visitFile(path: Path, attrs: BasicFileAttributes): FileVisitResult {
        if (path.isRegularFile()) {
          // Without the .toString(), endsWith checks if the last path segment equals the argument
          // instead.
          val kind = kinds.find { path.toString().endsWith(it.extension) }

          if (kind != null) {
            filesFound += InMemoryFileObject(location, path.toUri(), kind)
          }
        }

        return FileVisitResult.CONTINUE
      }
    }

    Files.walkFileTree(basePath, visitOpts, maxDepth, visitor)

    return filesFound
  }

  private fun fileToPath(location: Location, packageName: String, relativeName: String): Path {
    return this.rootPath
        .resolve(location.name)
        .resolve(packageName)
        .resolve(relativeName)
        .normalize()
        .toAbsolutePath()
  }

  private fun sourceToPath(location: Location, className: String, kind: Kind): Path {
    val classNameAsPath = className.removePrefix("/").replace('.', '/') + kind.extension

    return this.rootPath
        .resolve(location.name)
        .resolve(classNameAsPath)
        .normalize()
        .toAbsolutePath()
  }

  private fun createNewFileWithDirectories(path: Path) {
    path.parent.createDirectories()
    path.deleteIfExists()
    path.createFile()
  }

  private class VfsRootHandle(private val fs: FileSystem) : Runnable {
    override fun run() = this.fs.close()
  }

  companion object {
    // Garbage collector hook to free memory after we've finished with it.
    private val cleaner = Cleaner.create()

    // The standard locations to consider to be represented virtually.
    private val virtualLocations = setOf(
        StandardLocation.SOURCE_OUTPUT,
        StandardLocation.SOURCE_PATH,
        StandardLocation.CLASS_OUTPUT,
        StandardLocation.NATIVE_HEADER_OUTPUT,
        //StandardLocation.MODULE_SOURCE_PATH
    )

    /**
     * Create a [InMemoryFileManager] that wraps a given [JavaCompiler]'s
     * [StandardJavaFileManager], a [DiagnosticListener], a [Locale], and a [Charset].
     *
     * @param compiler the compiler to get the [StandardJavaFileManager] from.
     * @param diagnosticListener the diagnostic listener to use.
     * @param locale the locale to use.
     * @param charset the charset to use.
     * @return the wrapping [InMemoryFileManager].
     */
    @JvmStatic
    fun create(
        compiler: JavaCompiler,
        diagnosticListener: DiagnosticListener<JavaFileObject>,
        locale: Locale,
        charset: Charset
    ): InMemoryFileManager {
      val standardFileManager = compiler.getStandardFileManager(
          diagnosticListener,
          locale,
          charset
      )

      return InMemoryFileManager(standardFileManager)
    }
  }
}