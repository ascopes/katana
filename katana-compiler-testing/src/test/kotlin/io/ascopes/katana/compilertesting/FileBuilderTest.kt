package io.ascopes.katana.compilertesting

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayInputStream
import java.io.StringReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystem
import java.nio.file.Path
import java.util.UUID
import javax.tools.FileObject
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes
import kotlin.random.Random
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest

class FileBuilderTest {
  lateinit var fileBuilder: TestFileBuilder

  @BeforeEach
  fun setUp() {
    fileBuilder = TestFileBuilder()
  }

  @AfterEach
  fun tearDown() {
    fileBuilder.close()
  }

  @Test
  fun `create(String, ByteArray) creates a file`() {
    // Given
    val newFileName = "/foo/bar/${UUID.randomUUID()}"
    val content = Random.Default.nextBytes(30)

    // When
    fileBuilder.create(newFileName, content)

    // Then
    fileBuilder.withFsRoot {
      val path = resolve(newFileName)
      assertTrue(path.exists(), "$path did not exist")
      assertArrayEquals(content, path.readBytes())
    }
  }

  @Test
  fun `create(String, vararg String) creates a file`() {
    // Given
    val newFileName = "/foo/bar/${UUID.randomUUID()}"
    val contentLines = (0..10).map { UUID.randomUUID().toString() }.toTypedArray()
    val expectedFileBytes = contentLines
        .joinToString(separator = "\n")
        .toByteArray(StandardCharsets.UTF_8)

    // When
    fileBuilder.create(newFileName, *contentLines)

    // Then
    fileBuilder.withFsRoot {
      val path = resolve(newFileName)
      assertTrue(path.exists(), "$path did not exist")
      assertArrayEquals(expectedFileBytes, path.readBytes())
    }
  }

  @ParameterizedTest
  @Each.StandardCharset
  fun `create(String, vararg String, String, Charset) creates a file`(charset: Charset) {
    // Given
    val newFileName = "/foo/bar/${UUID.randomUUID()}"
    val contentLines = (0..10).map { UUID.randomUUID().toString() }.toTypedArray()
    val separator = UUID.randomUUID().toString()
    val expectedFileBytes = contentLines
        .joinToString(separator = separator)
        .toByteArray(charset)

    // When
    fileBuilder.create(newFileName, *contentLines, charset = charset, lineSeparator = separator)

    // Then
    fileBuilder.withFsRoot {
      val path = resolve(newFileName)
      assertTrue(path.exists(), "$path did not exist")
      assertArrayEquals(expectedFileBytes, path.readBytes())
    }
  }

  @Test
  fun `copyFromClassPath(String, String) will create the file from the current classpath`() {
    // Given
    val newFileName = "/foo/bar/${UUID.randomUUID()}"
    val classPathFile = "SomeClasspathResource.txt"
    val expectedFileBytes = this::class.java.classLoader.getResourceAsStream(classPathFile).use {
      it!!.readAllBytes()
    }

    // When
    fileBuilder.copyFromClassPath(
        classPathFile,
        newFileName
    )

    // Then
    fileBuilder.withFsRoot {
      val path = resolve(newFileName)
      assertTrue(path.exists(), "$path did not exist")
      assertArrayEquals(expectedFileBytes, path.readBytes())
    }
  }

  @Test
  fun `copyFromClassPath(ClassLoader, String, String) will create the file from the classpath`() {
    tempClassPath { root, urlClassLoader ->
      // Given
      val newFileName = "/foo/bar/${UUID.randomUUID()}"
      val classPathFile = UUID.randomUUID().toString()
      val expectedFileBytes = this::class.java.classLoader
          .getResourceAsStream("SomeClasspathResource.txt").use {
            it!!.readAllBytes()
          }

      root.resolve(classPathFile).writeBytes(expectedFileBytes)

      // When
      fileBuilder.copyFromClassPath(
          urlClassLoader,
          classPathFile,
          newFileName,
      )

      // Then
      fileBuilder.withFsRoot {
        val path = resolve(newFileName)
        assertTrue(path.exists(), "$path did not exist")
        assertArrayEquals(expectedFileBytes, path.readBytes())
      }
    }
  }

  @Test
  fun `copyFrom(Path, String) will create the file from the given path`() {
    tempDir { root ->
      // Given
      val newFileName = "/foo/bar/${UUID.randomUUID()}"
      val classPathFile = UUID.randomUUID().toString()
      val expectedFileBytes = this::class.java.classLoader
          .getResourceAsStream("SomeClasspathResource.txt").use {
            it!!.readAllBytes()
          }

      val filePath = root.resolve(classPathFile)
      filePath.writeBytes(expectedFileBytes)

      // When
      fileBuilder.copyFrom(filePath, newFileName)

      // Then
      fileBuilder.withFsRoot {
        val path = resolve(newFileName)
        assertTrue(path.exists(), "$path did not exist")
        assertArrayEquals(expectedFileBytes, path.readBytes())
      }
    }
  }

  @Test
  fun `copyFrom(FileObject, String) will create the file from the file object`() {
    // Given
    val newFileName = "/foo/bar/${UUID.randomUUID()}"

    val existingFileObject = mockk<FileObject>()
    val expectedFileBytes = Random.Default.nextBytes(30)
    every { existingFileObject.openInputStream() } answers { ByteArrayInputStream(expectedFileBytes) }

    // When
    fileBuilder.copyFrom(existingFileObject, newFileName)

    // Then
    fileBuilder.withFsRoot {
      val path = resolve(newFileName)
      assertTrue(path.exists(), "$path did not exist")
      assertArrayEquals(expectedFileBytes, path.readBytes())
    }
  }

  @Test
  fun `copyFrom(InputStream, String) will create the file from the InputStream`() {
    // Given
    val newFileName = "/foo/bar/${UUID.randomUUID()}"

    val expectedFileBytes = Random.Default.nextBytes(30)
    val inputStream = ByteArrayInputStream(expectedFileBytes)

    // When
    fileBuilder.copyFrom(inputStream, newFileName)

    // Then
    fileBuilder.withFsRoot {
      val path = resolve(newFileName)
      assertTrue(path.exists(), "$path did not exist")
      assertArrayEquals(expectedFileBytes, path.readBytes())
    }
  }

  @Test
  fun `copyFrom(Reader, String) will create the file from the Reader`() {
    // Given
    val newFileName = "/foo/bar/${UUID.randomUUID()}"

    val text = """
        Kotlin (/ˈkɒtlɪn/)[2] is a cross-platform, statically typed, general-purpose programming
        language with type inference. Kotlin is designed to interoperate fully with Java, and the 
        JVM version of Kotlin's standard library depends on the Java Class Library,[3] but type 
        inference allows its syntax to be more concise. Kotlin mainly targets the JVM, but also
        compiles to JavaScript (e.g., for frontend web applications using React[4]) or native
        code via LLVM (e.g., for native iOS apps sharing business logic with Android apps).[5]
        Language development costs are borne by JetBrains, while the Kotlin Foundation protects
        the Kotlin trademark.[6]
      """.trimIndent()

    val expectedFileBytes = text.toByteArray(StandardCharsets.UTF_8)

    val reader = StringReader(text)

    // When
    fileBuilder.copyFrom(reader, newFileName)

    // Then
    fileBuilder.withFsRoot {
      val path = resolve(newFileName)
      assertTrue(path.exists(), "$path did not exist")
      assertArrayEquals(expectedFileBytes, path.readBytes())
    }
  }

  @ParameterizedTest
  @Each.StandardCharset
  fun `copyFrom(Reader, String, Charset) will create the file from the Reader`(charset: Charset) {
    // Given
    val newFileName = "/foo/bar/${UUID.randomUUID()}"

    val text = """
        Kotlin (/ˈkɒtlɪn/)[2] is a cross-platform, statically typed, general-purpose programming
        language with type inference. Kotlin is designed to interoperate fully with Java, and the 
        JVM version of Kotlin's standard library depends on the Java Class Library,[3] but type 
        inference allows its syntax to be more concise. Kotlin mainly targets the JVM, but also
        compiles to JavaScript (e.g., for frontend web applications using React[4]) or native
        code via LLVM (e.g., for native iOS apps sharing business logic with Android apps).[5]
        Language development costs are borne by JetBrains, while the Kotlin Foundation protects
        the Kotlin trademark.[6]
      """.trimIndent()

    val expectedFileBytes = text.toByteArray(charset = charset)

    val reader = StringReader(text)

    // When
    fileBuilder.copyFrom(reader, newFileName, charset)

    // Then
    fileBuilder.withFsRoot {
      val path = resolve(newFileName)
      assertTrue(path.exists(), "$path did not exist")
      assertArrayEquals(expectedFileBytes, path.readBytes())
    }
  }

  @Test
  fun `copyFrom(URL, String) will create the file from the URL`() {
    tempDir { root ->
      // Given
      val newFileName = "/foo/bar/${UUID.randomUUID()}"
      val classPathFile = UUID.randomUUID().toString()
      val expectedFileBytes = this::class.java.classLoader
          .getResourceAsStream("SomeClasspathResource.txt").use {
            it!!.readAllBytes()
          }

      val filePath = root.resolve(classPathFile)
      filePath.writeBytes(expectedFileBytes)
      val fileUrl = filePath.toUri().toURL()

      // When
      fileBuilder.copyFrom(fileUrl, newFileName)

      // Then
      fileBuilder.withFsRoot {
        val path = resolve(newFileName)
        assertTrue(path.exists(), "$path did not exist")
        assertArrayEquals(expectedFileBytes, path.readBytes())
      }
    }
  }

  class TestFileBuilder : FileBuilder<TestFileBuilder>(), AutoCloseable {
    private val fs: FileSystem
    private val rootPath: Path

    init {
      val fsName = UUID.randomUUID().toString()
      val fs = Jimfs.newFileSystem(fsName, Configuration.unix())
      this.fs = fs
      this.rootPath = this.fs.getPath("/").toAbsolutePath()
    }

    override fun doCreate(newFileName: String, contentProvider: FileContentProvider) = apply {
      val fullPath = rootPath.resolve(newFileName)
      fullPath.parent.createDirectories()
      fullPath.createFile().writeBytes(contentProvider())
    }

    fun withFsRoot(expectations: Path.() -> Unit) {
      rootPath.expectations()
    }

    override fun close() {
      this.fs.close()
    }
  }
}