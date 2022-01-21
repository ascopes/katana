package io.ascopes.katana.compilertesting

import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.Reader
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import javax.tools.FileObject
import kotlin.io.path.readBytes

/**
 * Abstract base for a file builder.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 * @param F the implementation of this class.
 */
abstract class FileBuilder<F> : PolymorphicTypeSafeBuilder<F>()
    where F : FileBuilder<F> {

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

  /**
   * Implementation for how to create a file.
   *
   * @param newFileName the new file name to use.
   * @param contentProvider the supplier of a byte array. May throw any exception.
   */
  protected abstract fun doCreate(newFileName: String, contentProvider: FileContentProvider): F

  /**
   * Definition of a file content provider.
   */
  fun interface FileContentProvider {
    /**
     * Get the content and return it as a byte array.
     *
     * @throws IOException if something failed to be read.
     * @return the byte array of content.
     */
    @Throws(IOException::class)
    operator fun invoke(): ByteArray
  }
}