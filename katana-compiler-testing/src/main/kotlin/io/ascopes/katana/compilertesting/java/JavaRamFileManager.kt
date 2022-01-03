package io.ascopes.katana.compilertesting.java

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import me.xdrop.fuzzywuzzy.FuzzySearch
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
internal class JavaRamFileManager(
    standardFileManager: StandardJavaFileManager
) : ForwardingJavaFileManager<JavaFileManager>(standardFileManager) {
  private val fs: FileSystem
  private val rootPath: Path
  private val inMemoryLocations: Map<StandardLocation, JavaRamFileLocation>

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
  fun getOperationsFor(location: Location): JavaRamLocationOperations {
    val mappedLocation = this.locationFor(location)
        ?: throw UnsupportedOperationException("Location $location was not in-memory")

    return this.InMemoryLocationOperationsImpl(mappedLocation)
  }

  /**
   * Get the location operations for a module location.
   *
   * @param location the location.
   * @param moduleName the name of the module.
   * @return the location operations.
   */
  fun getOperationsFor(location: Location, moduleName: String): JavaRamLocationOperations {
    val mappedLocation = this.locationForModule(location, moduleName)
        ?: throw UnsupportedOperationException("Location $location was not non-module in-memory")

    return this.InMemoryLocationOperationsImpl(mappedLocation)
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
   * Determine if the location contains a given file object.
   *
   * @param location the location to check.
   * @param fileObject the file object to look for.
   * @return `true` if it exists and is within the location, `false` otherwise.
   */
  override fun contains(location: Location, fileObject: FileObject): Boolean {
    val mappedLocation = this.locationFor(location)
        ?: return super.contains(location, fileObject)

    return fileObject is JavaRamFileObject
        && fileObject.exists()
        && fileObject.toUri().toPath().startsWith(mappedLocation.path)
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
    val mappedLocation = this.locationFor(location)
        ?: return super.getFileForInput(location, packageName, relativeName)

    val fullRelativeName = Path.of(packageName, relativeName).toString()
    val path = this.fileToPath(mappedLocation, fullRelativeName)
    val obj = JavaRamFileObject(mappedLocation, path.toUri())
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
    val mappedLocation = this.locationFor(location)
        ?: return super.getJavaFileForInput(location, className, kind)

    val path = this.sourceToPath(mappedLocation, className, kind)
    val obj = JavaRamFileObject(mappedLocation, path.toUri())
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
    val mappedLocation = this.locationFor(location)
        ?: return super.getFileForOutput(location, packageName, relativeName, sibling)

    val fullRelativeName = Path.of(packageName, relativeName).toString()
    val path = this.fileToPath(mappedLocation, fullRelativeName)
    this.createNewFileWithDirectories(path)
    return JavaRamFileObject(mappedLocation, path.toUri())
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
    val mappedLocation = this.locationFor(location)
        ?: return super.getJavaFileForOutput(location, className, kind, sibling)

    val path = this.sourceToPath(mappedLocation, className, kind)
    this.createNewFileWithDirectories(path)
    return JavaRamFileObject(mappedLocation, path.toUri())
  }

  /**
   * Get the location for a module.
   *
   * @param location the location all modules are held in.
   * @param moduleName the name of the module.
   * @return the location of the module.
   */
  override fun getLocationForModule(location: Location, moduleName: String): Location {
    return this.locationForModule(location, moduleName)
        ?: super.getLocationForModule(location, moduleName)
  }

  /**
   * Get the location for the module that the given file object is in.
   *
   * @param location the location that all modules are held in.
   * @param fileObject the file object for an element in the module.
   * @return the location of the module.
   */
  override fun getLocationForModule(location: Location, fileObject: JavaFileObject): Location {
    val mappedLocation = this.locationFor(location)
        ?: return super.getLocationForModule(location, fileObject)

    if (!mappedLocation.isModuleOrientedLocation) {
      // Multi-module is enabled but the location doesn't have nested modules, I guess.
      // TODO: check if this is the correct behaviour?
      return mappedLocation
    }

    // The first directory in the module-oriented location
    val moduleName = mappedLocation
        .path
        .relativize(fileObject.toUri().toPath())
        .first()
        .toString()

    // The first nested directory is the module name.
    // Remember module-oriented means it contains module locations, not that the location
    // itself is of a module.
    return this.getLocationForModule(location, moduleName)
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

    val mappedLocation = this.locationFor(location)
    return mappedLocation != null
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
    val mappedLocation = this.locationFor(location)
        ?: return super.inferBinaryName(location, file)

    return file.toUri().toPath().relativeTo(mappedLocation.path)
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
    val mappedLocation = this.locationFor(location)

    if (mappedLocation is JavaRamModuleLocation) {
      return mappedLocation.moduleName
    }

    return super.inferModuleName(location)
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
    val mappedLocation = this.locationFor(location)
        ?: return super.list(location, packageName, kinds, recurse)

    val basePath = this.sourceToPath(mappedLocation, packageName, Kind.OTHER)

    if (basePath.notExists()) {
      // Package does not exist yet, probably.
      return emptyList()
    }

    val visitOpts = emptySet<FileVisitOption>()
    val maxDepth = if (recurse) Int.MAX_VALUE else 1
    val filesFound = mutableListOf<JavaRamFileObject>()

    val visitor = object : SimpleFileVisitor<Path>() {
      override fun visitFile(path: Path, attrs: BasicFileAttributes): FileVisitResult {
        if (path.isRegularFile()) {
          // Without the .toString(), endsWith checks if the last path segment equals the argument
          // instead.
          val kind = kinds.find { path.toString().endsWith(it.extension) }

          if (kind != null) {
            filesFound += JavaRamFileObject(mappedLocation, path.toUri(), kind)
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
    val mappedLocation = this.locationFor(location)
        ?: return super.listLocationsForModules(location)

    val path = mappedLocation.path

    val files = Files
        .list(path)
        .filter { it.isDirectory() }
        .filter { dir ->
          Files
              .list(dir)
              .filter { it.isRegularFile() }
              .anyMatch { it.fileName.toString() == "module-info.java" }
        }
        .map { JavaRamModuleLocation(mappedLocation, it, it.fileName.toString()) }
        .asSequence()
        .toSet()

    return listOf(files)
  }

  private fun sourceToPath(location: JavaRamLocation, className: String, kind: Kind): Path {
    val classNameAsPath = this.classNameAsPath(className, kind)

    return location
        .path
        .resolve(classNameAsPath)
        .normalize()
        .toAbsolutePath()
  }

  private fun fileToPath(location: JavaRamLocation, relativeName: String): Path {
    return location
        .path
        .resolve(relativeName)
        .normalize()
        .toAbsolutePath()
  }

  private fun classNameAsPath(className: String, kind: Kind) = className
      .removePrefix("/")
      .replace('.', '/') + kind.extension

  private fun locationFor(parent: Location): JavaRamLocation? {
    return when (parent) {
      is JavaRamLocation -> parent
      in this.inMemoryLocations -> this.inMemoryLocations[parent]!!
      else -> null
    }
  }

  private fun locationForModule(parent: Location, moduleName: String): JavaRamModuleLocation? {
    val mappedParent = when (parent) {
      is JavaRamFileLocation -> parent
      in this.inMemoryLocations -> this.inMemoryLocations[parent]!!
      else -> return null
    }

    val modulePath = mappedParent.path.resolve(moduleName)
    return JavaRamModuleLocation(mappedParent, modulePath, moduleName)
  }

  private fun createNewFileWithDirectories(path: Path) {
    path.parent.createDirectories()
    path.deleteIfExists()
    path.createFile()
  }

  private fun mapLocationFor(location: StandardLocation, dirName: String) =
      location to JavaRamFileLocation(
          location,
          this.rootPath.resolve(dirName)
      )

  private fun findClosestFileNameMatchesFor(location: Location, fileName: String): List<String> {
    val mappedLocation = this.locationFor(location)
        ?: return emptyList()

    val files = this
        .list(mappedLocation, "", setOf(Kind.OTHER), true)
        .map { it.toUri().toPath().relativeTo(mappedLocation.path).toString() }

    return FuzzySearch
        .extractTop(fileName, files, 3, 70)
        .map { it.string }
        .toList()
  }

  private inner class InMemoryLocationOperationsImpl(
      override val location: JavaRamLocation
  ) : JavaRamLocationOperations {

    override val moduleName: String?
      get() = if (this.location is JavaRamModuleLocation) {
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

    override fun getFile(fileName: String): JavaRamFileObject? {
      val path = this@JavaRamFileManager.fileToPath(this.location, fileName)
      val obj = JavaRamFileObject(this.location, path.toUri())
      return if (obj.exists()) obj else null
    }

    override fun findClosestFileNamesTo(fileName: String) = this@JavaRamFileManager
        .findClosestFileNameMatchesFor(this.location, fileName)

    private fun createFile(fileName: String): Path {
      val path = this@JavaRamFileManager.fileToPath(this.location, fileName)
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
     * Create a [JavaRamFileManager] that wraps a given [JavaCompiler]'s
     * [StandardJavaFileManager], a [DiagnosticListener], a [Locale], and a [Charset].
     *
     * @param compiler the compiler to get the [StandardJavaFileManager] from.
     * @param diagnosticListener the diagnostic listener to use.
     * @param locale the locale to use.
     * @param charset the charset to use.
     * @return the wrapping [JavaRamFileManager].
     */
    @JvmStatic
    fun create(
        compiler: JavaCompiler,
        diagnosticListener: DiagnosticListener<JavaFileObject>,
        locale: Locale,
        charset: Charset
    ): JavaRamFileManager {
      val standardFileManager = compiler.getStandardFileManager(
          diagnosticListener,
          locale,
          charset
      )

      return JavaRamFileManager(standardFileManager)
    }
  }
}