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
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.notExists
import kotlin.io.path.relativeTo
import kotlin.io.path.toPath
import kotlin.io.path.writeBytes
import kotlin.io.path.writeLines
import kotlin.streams.asSequence


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
 * This also supports basic multimodule compilation, at the time of writing...
 *
 * @author Ashley Scopes
 * @since 0.1.0
 * @param standardFileManager the compiler-supplied standard file manager to delegate most calls to.
 */
internal class InMemoryFileManager(
    standardFileManager: StandardJavaFileManager
) : ForwardingJavaFileManager<JavaFileManager>(standardFileManager) {
  private val fs: FileSystem
  private val rootPath: Path
  private val inMemoryLocations: Map<StandardLocation, InMemoryFileLocation>

  init {
    val fsName = UUID.randomUUID().toString()
    this.fs = Jimfs.newFileSystem(fsName, Configuration.unix())
    this.rootPath = this.fs.getPath("/").toAbsolutePath()

    this.inMemoryLocations = mapOf(
        this.mapLocationFor(StandardLocation.SOURCE_PATH, "input/main"),
        this.mapLocationFor(StandardLocation.MODULE_SOURCE_PATH, "input/modules"),
        this.mapLocationFor(StandardLocation.SOURCE_OUTPUT, "output/sources"),
        this.mapLocationFor(StandardLocation.CLASS_OUTPUT, "output/classes"),
        this.mapLocationFor(StandardLocation.NATIVE_HEADER_OUTPUT, "output/headers"),
    )

    this.inMemoryLocations.values.forEach { it.path.createDirectories() }

    // On garbage collection, destroy any created files within the temporary root we created.
    Companion.cleaner.register(this, VfsRootHandle(this.fs))
  }

  /**
   * Get the location operations for a non-module location.
   *
   * @param location the location.
   * @return the location operations.
   */
  fun getLocationFor(location: Location): InMemoryLocationOperations {
    if (location !is InMemoryLocation) {
      val mappedLocation = this.inMemoryLocations[location]
      if (mappedLocation == null) {
        throw UnsupportedOperationException("Can only handle in-memory locations, not $location")
      } else {
        return this.getLocationFor(mappedLocation)
      }
    }

    return this.InMemoryLocationOperationsImpl(location)
  }

  /**
   * Get the location operations for a module location.
   *
   * @param location the location.
   * @param moduleName the name of the module.
   * @return the location operations.
   */
  fun getLocationFor(location: Location, moduleName: String): InMemoryLocationOperations {
    if (location !is InMemoryLocation) {
      val mappedLocation = this.inMemoryLocations[location]
      if (mappedLocation == null) {
        throw UnsupportedOperationException("Can only handle in-memory locations, not $location")
      } else {
        return this.getLocationFor(mappedLocation, moduleName)
      }
    }

    val modulePath = location.path.resolve(moduleName)
    val moduleLocation = InMemoryModuleLocation(location, modulePath, moduleName)
    return this.InMemoryLocationOperationsImpl(moduleLocation)
  }

  /**
   * Get an iterable across all in-memory files.
   *
   * @return the iterable sequence.
   */
  fun listAllInMemoryFiles(): Iterable<Path> {
    val files = mutableSetOf<Path>()

    val visitor = object : SimpleFileVisitor<Path>() {
      override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
        files.add(file.toAbsolutePath())
        return FileVisitResult.CONTINUE
      }
    }

    Files.walkFileTree(this.rootPath, visitor)

    return files
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
    if (location !is InMemoryLocation) {
      val mappedLocation = this.inMemoryLocations[location]
      return if (mappedLocation == null) {
        super.getFileForInput(location, packageName, relativeName)
      } else {
        this.getFileForInput(mappedLocation, packageName, relativeName)
      }
    }

    val path = this.fileToPath(location, "$packageName/$relativeName")
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
    if (location !is InMemoryLocation) {
      val mappedLocation = this.inMemoryLocations[location]
      return if (mappedLocation == null) {
        super.getJavaFileForInput(location, className, kind)
      } else {
        this.getJavaFileForInput(mappedLocation, className, kind)
      }
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
    if (location !is InMemoryLocation) {
      val mappedLocation = this.inMemoryLocations[location]
      return if (mappedLocation == null) {
        super.getFileForOutput(location, packageName, relativeName, sibling)
      } else {
        this.getFileForOutput(mappedLocation, packageName, relativeName, sibling)
      }
    }

    val path = this.fileToPath(location, Path.of(packageName, relativeName).toString())
    this.createNewFileWithDirectories(path)
    return InMemoryFileObject(location, path.toUri())
  }


  /**
   * Get a source file for use as an output.
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
    if (location !is InMemoryLocation) {
      val mappedLocation = this.inMemoryLocations[location]
      return if (mappedLocation == null) {
        super.getJavaFileForOutput(location, className, kind, sibling)
      } else {
        this.getJavaFileForOutput(mappedLocation, className, kind, sibling)
      }
    }

    val path = this.sourceToPath(location, className, kind)
    this.createNewFileWithDirectories(path)
    return InMemoryFileObject(location, path.toUri())
  }

  /**
   * Get the location for a module.
   *
   * @param location the location all modules are held in.
   * @param moduleName the name of the module.
   * @return the location of the module.
   */
  override fun getLocationForModule(location: Location, moduleName: String): Location {
    if (location !is InMemoryLocation) {
      val mappedLocation = this.inMemoryLocations[location]
      return if (mappedLocation == null) {
        super.getLocationForModule(location, moduleName)
      } else {
        this.getLocationForModule(mappedLocation, moduleName)
      }
    }

    return InMemoryModuleLocation(location, location.path.resolve(moduleName), moduleName)
  }

  /**
   * Get the location for the module that the given file object is in.
   *
   * @param location the location that all modules are held in.
   * @param fileObject the file object for an element in the module.
   * @return the location of the module.
   */
  override fun getLocationForModule(location: Location, fileObject: JavaFileObject): Location {
    if (location !is InMemoryLocation) {
      val mappedLocation = this.inMemoryLocations[location]
      return if (mappedLocation == null) {
        super.getLocationForModule(location, fileObject)
      } else {
        this.getLocationForModule(mappedLocation, fileObject)
      }
    }

    if (location.isModuleOrientedLocation) {
      val moduleName = location.path.relativize(fileObject.toUri().toPath()).first().toString()

      // The first nested directory is the module name.
      // Modules themselves cannot be module-oriented for whatever reason.
      return this.getLocationForModule(location, moduleName)
    }

    return location
  }

  /**
   * Determine if the given location exists or not.
   *
   * @param location the location to look up.
   * @return true if the location exists, or false otherwise.
   */
  override fun hasLocation(location: Location): Boolean {
    if (location == StandardLocation.MODULE_SOURCE_PATH) {
      // We flag this to implicitly enable this feature when we want it. Otherwise, it results in
      // our source paths being moved around for non-modular compilation which causes issues
      // elsewhere for us.
      val path = this.inMemoryLocations[StandardLocation.MODULE_SOURCE_PATH]!!.path
      return Files.list(path).count() > 0
    }

    if (location is InMemoryLocation) {
      return true
    }

    val mappedLocation = this.inMemoryLocations[location]

    return if (mappedLocation == null) {
      super.hasLocation(location)
    } else {
      this.hasLocation(mappedLocation)
    }
  }

  /**
   * Determine if two file objects are considered to be the same file.
   *
   * @param a the first file.
   * @param b the second file.
   */
  override fun isSameFile(a: FileObject, b: FileObject): Boolean {
    return a.toUri() == b.toUri()
  }

  /**
   * Infer the canonical name for a class represented in a Java class file.
   *
   * @param location the location of the file.
   * @param file the source file.
   * @return the canonical name.
   */
  override fun inferBinaryName(location: Location, file: JavaFileObject): String {
    if (location !is InMemoryLocation) {
      val mappedLocation = this.inMemoryLocations[location]
      return if (mappedLocation == null) {
        super.inferBinaryName(location, file)
      } else {
        this.inferBinaryName(mappedLocation, file)
      }
    }

    return file.toUri().toPath().relativeTo(location.path)
        .toString()
        .removeSuffix(file.kind.extension)
        .replace('/', '.')
  }

  /**
   * Infer the correct module name for a location that has a module bound to it.
   *
   * @param location the location.
   * @return the module name.
   */
  override fun inferModuleName(location: Location): String {
    return if (location is InMemoryModuleLocation) {
      location.moduleName
    } else {
      super.inferModuleName(location)
    }
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
    if (location !is InMemoryLocation) {
      val mappedLocation = this.inMemoryLocations[location]
      return if (mappedLocation == null) {
        super.list(location, packageName, kinds, recurse)
      } else {
        this.list(mappedLocation, packageName, kinds, recurse)
      }
    }

    val basePath = this.sourceToPath(location, packageName, Kind.OTHER)

    if (basePath.notExists()) {
      // Package does not exist yet, probably.
      return emptyList()
    }

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

  /**
   * List the locations for modules in the given location.
   *
   * @param location the location to consider.
   * @return the iterable across the set of locations.
   */
  override fun listLocationsForModules(location: Location): Iterable<Set<Location>> {
    if (location !is InMemoryLocation) {
      val mappedLocation = this.inMemoryLocations[location]
      return if (mappedLocation == null) {
        super.listLocationsForModules(location)
      } else {
        this.listLocationsForModules(mappedLocation)
      }
    }

    val path = location.path

    val files = Files
        .list(path)
        .filter { it.isDirectory() }
        .filter { dir ->
          Files
              .list(dir)
              .filter { it.isRegularFile() }
              .anyMatch { it.fileName.toString() == "module-info.java" }
        }
        .map { InMemoryModuleLocation(location, it, it.fileName.toString()) }
        .asSequence()
        .toSet()

    return listOf(files)
  }

  private fun sourceToPath(location: InMemoryLocation, className: String, kind: Kind): Path {
    val classNameAsPath = this.classNameAsPath(className, kind)

    return location
        .path
        .resolve(classNameAsPath)
        .normalize()
        .toAbsolutePath()
  }

  private fun fileToPath(location: InMemoryLocation, relativeName: String): Path {
    return location
        .path
        .resolve(relativeName)
        .normalize()
        .toAbsolutePath()
  }

  private fun classNameAsPath(className: String, kind: Kind) = className
      .removePrefix("/")
      .replace('.', '/') + kind.extension

  private fun createNewFileWithDirectories(path: Path) {
    path.parent.createDirectories()
    path.deleteIfExists()
    path.createFile()
  }

  private fun mapLocationFor(location: StandardLocation, dirName: String) =
      location to InMemoryFileLocation(
          location,
          this.rootPath.resolve(dirName)
      )

  private inner class InMemoryLocationOperationsImpl(
      override val location: InMemoryLocation
  ) : InMemoryLocationOperations {

    override val moduleName: String?
      get() = if (this.location is InMemoryModuleLocation) {
        this.location.moduleName
      } else {
        null
      }

    override val path: Path
      get() = this.location.path

    override fun createFile(fileName: String, content: ByteArray) = this
        .createFile(fileName)
        .apply { this.writeBytes(content) }

    override fun createFile(fileName: String, vararg lines: String) = this
        .createFile(fileName)
        .apply { this.writeLines(lines.asSequence()) }

    override fun getFile(fileName: String): InMemoryFileObject? {
      val path = this@InMemoryFileManager.fileToPath(this.location, fileName)
      val obj = InMemoryFileObject(this.location, path.toUri())
      return if (obj.exists()) obj else null
    }

    private fun createFile(fileName: String): Path {
      val path = this@InMemoryFileManager.fileToPath(this.location, fileName)
      path.parent.createDirectories()
      return path.createFile()
    }
  }

  private class VfsRootHandle(private val fs: FileSystem) : Runnable {
    override fun run() = this.fs.close()
  }

  companion object {
    // Garbage collector hook to free memory after we've finished with it.
    private val cleaner = Cleaner.create()

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