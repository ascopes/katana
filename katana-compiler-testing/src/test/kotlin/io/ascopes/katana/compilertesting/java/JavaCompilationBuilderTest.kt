package io.ascopes.katana.compilertesting.java

import io.ascopes.katana.compilertesting.Each
import io.ascopes.katana.compilertesting.StackTraceProvider
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.Locale
import java.util.UUID
import javax.annotation.processing.Processor
import javax.lang.model.SourceVersion
import javax.tools.JavaCompiler
import javax.tools.JavaFileObject
import javax.tools.StandardJavaFileManager
import javax.tools.StandardLocation
import javax.tools.ToolProvider
import kotlin.io.path.absolutePathString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class JavaCompilationBuilderTest {
  @Test
  fun `includeModules(vararg String) adds the modules to the module path`() {
    val builder = this.mockedBuilder()
    builder.modules += setOf("foo", "bar", "baz")

    builder.includeModules("aaa", "bbb", "ccc")

    assertEquals(setOf("foo", "bar", "baz", "aaa", "bbb", "ccc"), builder.modules)
  }

  @ParameterizedTest
  @ValueSource(ints = [11, 12, 13, 14, 15, 16, 17, 18, 19, 20])
  fun `sourceVersion(int) sets the source version flag`(version: Int) {
    val builder = this.mockedBuilder()
    builder.options += listOf("foo", "bar", "baz")

    builder.sourceVersion(version)

    assertEquals(listOf("foo", "bar", "baz", JavaCompilationBuilder.SOURCE_FLAG, "$version"), builder.options)
  }

  @ParameterizedTest
  @Each.JavaVersion
  fun `sourceVersion(SourceVersion) sets the source version flag`(version: SourceVersion) {
    val builder = this.mockedBuilder()
    builder.options += listOf("aaaaa", "bbb")

    builder.sourceVersion(version)

    val expectedVersion = Regex("^RELEASE_(\\d+)$")
        .matchEntire(version.name)!!
        .groupValues[1]

    assertEquals(
        listOf("aaaaa", "bbb", JavaCompilationBuilder.SOURCE_FLAG, expectedVersion),
        builder.options
    )
  }

  @ParameterizedTest
  @ValueSource(ints = [11, 12, 13, 14, 15, 16, 17, 18, 19, 20])
  fun `targetVersion(int) sets the target version flag`(version: Int) {
    val builder = this.mockedBuilder()
    builder.options += listOf("eggs", "spam")

    builder.targetVersion(version)

    assertEquals(
        listOf("eggs", "spam", JavaCompilationBuilder.TARGET_FLAG, "$version"),
        builder.options
    )
  }

  @ParameterizedTest
  @Each.JavaVersion
  fun `targetVersion(SourceVersion) sets the target version flag`(version: SourceVersion) {
    val builder = this.mockedBuilder()
    builder.options += listOf("xxx", "yyy", "zzz", "aaa")

    builder.targetVersion(version)

    val expectedVersion = Regex("^RELEASE_(\\d+)$")
        .matchEntire(version.name)!!
        .groupValues[1]

    assertEquals(
        listOf("xxx", "yyy", "zzz", "aaa", JavaCompilationBuilder.TARGET_FLAG, expectedVersion),
        builder.options
    )
  }

  @ParameterizedTest
  @ValueSource(ints = [11, 12, 13, 14, 15, 16, 17, 18, 19, 20])
  fun `releaseVersion(int) sets the release version flag`(version: Int) {
    val builder = this.mockedBuilder()
    builder.options += listOf("this", "is", "my", "release")

    builder.releaseVersion(version)

    assertEquals(
        listOf("this", "is", "my", "release", JavaCompilationBuilder.RELEASE_FLAG, "$version"),
        builder.options
    )
  }

  @ParameterizedTest
  @Each.JavaVersion
  fun `releaseVersion(SourceVersion) sets the release version flag`(version: SourceVersion) {
    val builder = this.mockedBuilder()
    builder.options += listOf("yawn")

    builder.releaseVersion(version)

    val expectedVersion = Regex("^RELEASE_(\\d+)$")
        .matchEntire(version.name)!!
        .groupValues[1]

    assertEquals(
        listOf("yawn", JavaCompilationBuilder.RELEASE_FLAG, expectedVersion),
        builder.options
    )
  }

  @ParameterizedTest
  @ValueSource(ints = [11, 12, 13, 14, 15, 16, 17, 18, 19, 20])
  fun `sourceAndTargetVersion(int) sets the source and target version flags`(version: Int) {
    val builder = this.mockedBuilder()
    builder.options += listOf("foo", "bar", "baz")

    builder.sourceAndTargetVersion(version)

    assertEquals(
        listOf(
            "foo", "bar", "baz",
            JavaCompilationBuilder.SOURCE_FLAG, "$version",
            JavaCompilationBuilder.TARGET_FLAG, "$version"
        ),
        builder.options
    )
  }

  @ParameterizedTest
  @Each.JavaVersion
  fun `sourceAndTargetVersion(SourceVersion) sets the source and target version flags`(
      version: SourceVersion
  ) {
    val builder = this.mockedBuilder()
    builder.options += listOf("aaaaa", "bbb")

    builder.sourceAndTargetVersion(version)

    val expectedVersion = Regex("^RELEASE_(\\d+)$")
        .matchEntire(version.name)!!
        .groupValues[1]

    assertEquals(
        listOf(
            "aaaaa", "bbb",
            JavaCompilationBuilder.SOURCE_FLAG, expectedVersion,
            JavaCompilationBuilder.TARGET_FLAG, expectedVersion
        ),
        builder.options
    )
  }

  @Test
  fun `options() appends to the list of options`() {
    val builder = this.mockedBuilder()
    builder.options += listOf("drum", "n", "bass")

    builder.options("--foo", "--bar", "--baz 12")

    assertEquals(
        listOf("drum", "n", "bass", "--foo", "--bar", "--baz 12"),
        builder.options
    )
  }

  @Test
  fun `processors() appends to the list of processors`() {
    val builder = this.mockedBuilder()
    val processor1 = mockk<Processor>("processor1")
    val processor2 = mockk<Processor>("processor2")
    val processor3 = mockk<Processor>("processor3")
    builder.processors += listOf(processor1, processor2, processor3)

    val processor4 = mockk<Processor>("processor4")
    val processor5 = mockk<Processor>("processor5")

    builder.processors(processor4, processor5)

    assertEquals(
        listOf(processor1, processor2, processor3, processor4, processor5),
        builder.processors
    )
  }

  @Test
  fun `treatWarningsAsErrors() adds the -Werror flag to the options`() {
    val builder = this.mockedBuilder()
    builder.options += listOf("some", "existing", "stuff", "here")

    builder.treatWarningsAsErrors()

    assertEquals(
        listOf("some", "existing", "stuff", "here", JavaCompilationBuilder.WERROR_FLAG),
        builder.options
    )
  }

  @Test
  fun `generateHeaders() adds the -h flat to the options`() {
    // given
    val standardLoc = StandardLocation.NATIVE_HEADER_OUTPUT
    val fileManagerMock = mockk<JavaRamFileManager>()
    val builder = this.mockedBuilder(fileManager = fileManagerMock)
    val locationMock = mockk<JavaRamLocation>()
    val pathMock = mockk<Path>()
    val headerPath = "/foo/bar/${UUID.randomUUID()}"

    every { fileManagerMock.getInMemoryLocationFor(standardLoc) } returns locationMock
    every { locationMock.path } returns pathMock
    every { pathMock.absolutePathString() } returns headerPath

    builder.options += listOf("carbonated", "low", "calorie", "cola")

    // when
    builder.generateHeaders()

    // then
    verify { fileManagerMock.getInMemoryLocationFor(standardLoc) }
    verify { locationMock.path }
    verify { pathMock.absolutePathString() }

    assertEquals(
        listOf(
            "carbonated",
            "low",
            "calorie",
            "cola",
            JavaCompilationBuilder.HEADER_FLAG,
            headerPath
        ),
        builder.options
    )
  }

  @Nested
  @ExtendWith(MockKExtension::class)
  inner class CompileTest {
    @MockK
    private lateinit var fileManager: JavaRamFileManager

    @MockK
    private lateinit var compiler: JavaCompiler

    @MockK
    private lateinit var compilationTask: JavaCompiler.CompilationTask

    @MockK
    private lateinit var diagnosticListener: JavaDiagnosticListener

    private lateinit var builder: JavaCompilationBuilder

    @BeforeEach
    fun setUp() {
      every { compiler.getTask(any(), any(), any(), any(), any(), any()) } returns compilationTask
      every { fileManager.list(any(), any(), any(), any()) } returns emptyList()
      every { fileManager.listLocationsForModules(any()) } returns emptyList()
      every { fileManager.setClassPath(any()) } answers { }
      every { compilationTask.addModules(any()) } answers { }
      every { compilationTask.setProcessors(any()) } answers { }
      every { compilationTask.setLocale(any()) } answers { }
      every { diagnosticListener.diagnostics } returns emptyList()

      this.builder = JavaCompilationBuilder(compiler, diagnosticListener, fileManager)
    }

    @Test
    fun `the compiler task generation uses the file manager`() {
      // When
      builder.compile()

      // Then
      verifyOrder {
        compiler.getTask(
            any(),
            refEq(fileManager),
            any(),
            any(),
            any(),
            any()
        )
        compilationTask.call()
      }
    }

    @Test
    fun `the compiler task generation uses the diagnostic listener`() {
      // When
      builder.compile()

      // Then
      verifyOrder {
        compiler.getTask(
            any(),
            any(),
            refEq(diagnosticListener),
            any(),
            any(),
            any()
        )
        compilationTask.call()
      }
    }

    @Test
    fun `the compiler task generation uses the builder options`() {
      // Given
      builder.options += listOf("ayy", "bee", "cee", "-Werror")

      // When
      builder.compile()

      // Then
      verifyOrder {
        compiler.getTask(
            any(),
            any(),
            any(),
            eq(listOf("ayy", "bee", "cee", "-Werror")),
            any(),
            any()
        )
        compilationTask.call()
      }
    }

    @Test
    fun `the compiler task generation uses a null list of classes to preprocess explicitly`() {
      // When
      builder.compile()

      // Then
      verifyOrder {
        compiler.getTask(
            any(),
            any(),
            any(),
            any(),
            isNull(),
            any()
        )
        compilationTask.call()
      }
    }

    @Test
    fun `the compiler task generation uses the expected compilation units`() {
      // Given
      val nonModuleFile1 = mockk<JavaRamFileObject>()
      val nonModuleFile2 = mockk<JavaRamFileObject>()
      val nonModuleFile3 = mockk<JavaRamFileObject>()

      val moduleLocation1 = mockk<JavaRamModuleLocation>()
      val moduleFile1a = mockk<JavaRamFileObject>()
      val moduleFile1b = mockk<JavaRamFileObject>()

      val moduleLocation2 = mockk<JavaRamModuleLocation>()
      val moduleFile2a = mockk<JavaRamFileObject>()

      val moduleLocation3 = mockk<JavaRamModuleLocation>()
      val moduleFile3a = mockk<JavaRamFileObject>()
      val moduleFile3b = mockk<JavaRamFileObject>()
      val moduleFile3c = mockk<JavaRamFileObject>()

      every {
        fileManager.list(refEq(StandardLocation.SOURCE_PATH), any(), any(), any())
      } returns listOf(nonModuleFile1, nonModuleFile2, nonModuleFile3)

      every {
        fileManager.list(refEq(moduleLocation1), any(), any(), any())
      } returns listOf(moduleFile1a, moduleFile1b)

      every {
        fileManager.list(refEq(moduleLocation2), any(), any(), any())
      } returns listOf(moduleFile2a)

      every {
        fileManager.list(refEq(moduleLocation3), any(), any(), any())
      } returns listOf(moduleFile3a, moduleFile3b, moduleFile3c)

      every {
        fileManager.listLocationsForModules(refEq(StandardLocation.MODULE_SOURCE_PATH))
        // In reality, we always would expect a single element, but that is internal detail
        // that is just the side effect of how our in-memory file manager works, so we don't rely
        // on it here.
      } returns listOf(setOf(moduleLocation1, moduleLocation2), setOf(moduleLocation3))

      // When
      builder.compile()

      val expectedCompilationUnits = listOf(
          nonModuleFile1, nonModuleFile2, nonModuleFile3,
          moduleFile1a, moduleFile1b,
          moduleFile2a,
          moduleFile3a, moduleFile3b, moduleFile3c
      )

      // Then
      val actualCompilationUnitsSlot = slot<Iterable<JavaFileObject>>()

      verifyOrder {
        compiler.getTask(any(), any(), any(), any(), any(), capture(actualCompilationUnitsSlot))
        compilationTask.call()
      }

      val actualCompilationUnits = actualCompilationUnitsSlot.captured.toList()

      assertEquals(expectedCompilationUnits.size, actualCompilationUnits.size)

      expectedCompilationUnits.forEach { expected ->
        assertEquals(
            1,
            actualCompilationUnitsSlot.captured.count { it === expected },
            "Expected = $expectedCompilationUnits, Actual = $actualCompilationUnits"
        )
      }
    }

    @Test
    fun `the compilation task uses the root locale`() {
      // When
      builder.compile()

      // Then
      verifyOrder {
        compilationTask.setLocale(Locale.ROOT)
        compilationTask.call()
      }
    }

    @Test
    fun `the compilation task has the processors added`() {
      // Given
      val p1 = mockk<Processor>()
      val p2 = mockk<Processor>()
      val p3 = mockk<Processor>()
      val p4 = mockk<Processor>()

      builder.processors += listOf(p1, p2, p3, p4)

      // When
      builder.compile()

      // Then
      verifyOrder {
        compilationTask.setProcessors(listOf(p1, p2, p3, p4))
        compilationTask.call()
      }
    }

    @Test
    fun `the compilation task has the modules added`() {
      // Given
      builder.modules += setOf("java.compiler", "java.desktop", "java.net.http")

      // When
      builder.compile()

      // Then
      verifyOrder {
        compilationTask.addModules(listOf("java.compiler", "java.desktop", "java.net.http"))
        compilationTask.call()
      }
    }

    @Test
    fun `the compilation result is successful when compilation succeeds`() {
      // Given
      every { compilationTask.call() } returns true

      // When
      val compilation = builder.compile()

      // Then
      assertTrue(compilation.result.isSuccess)
    }

    @Test
    fun `the compilation result is a failure when compilation fails`() {
      // Given
      every { compilationTask.call() } returns false

      // When
      val compilation = builder.compile()

      // Then
      assertTrue(compilation.result.isFailure)
    }

    @Test
    fun `the compilation result is an exception when compilation throws an exception`() {
      // Given
      val exception = RuntimeException("Stuffes broken yo")
      every { compilationTask.call() } throws exception

      // When
      val compilation = builder.compile()

      // Then
      assertTrue(compilation.result.isException)
      assertSame(exception, compilation.result.exception)
    }

    @Test
    fun `the compilation result uses the modules`() {
      // Given
      builder.modules += setOf("java.compiler", "java.desktop", "java.net.http")

      // When
      val compilation = builder.compile()

      // Then
      assertEquals(setOf("java.compiler", "java.desktop", "java.net.http"), compilation.modules)
    }

    @Test
    fun `the compilation result uses the processors`() {
      // Given
      val p1 = mockk<Processor>()
      val p2 = mockk<Processor>()
      val p3 = mockk<Processor>()
      val p4 = mockk<Processor>()

      builder.processors += listOf(p1, p2, p3, p4)

      // When
      val compilation = builder.compile()

      // Then
      assertEquals(listOf(p1, p2, p3, p4), compilation.processors)
    }

    @Test
    fun `the compilation result uses the diagnostics`() {
      // Given
      val diagnostics = mockk<List<JavaRamDiagnostic<out JavaFileObject>>>()
      every { diagnosticListener.diagnostics } returns diagnostics

      // When
      val compilation = builder.compile()

      // Then
      assertSame(diagnostics, compilation.diagnostics)
    }

    @Test
    fun `the compilation result uses the options`() {
      // Given
      builder.options += listOf("ayy", "bee", "cee", "-Werror")

      // When
      val compilation = builder.compile()

      // Then
      assertEquals(listOf("ayy", "bee", "cee", "-Werror"), compilation.options)
    }

    @Test
    fun `the compilation result uses the file manager`() {
      // When
      val compilation = builder.compile()

      // Then
      assertSame(fileManager, compilation.fileManager)
    }
  }

  @Nested
  inner class JavaFileBuilderTest {
    @Test
    fun `and() returns the compilation builder`() {
      val parentBuilder = this@JavaCompilationBuilderTest.mockedBuilder()
      val fileBuilder = parentBuilder.JavaFileBuilder(mockk())
      assertSame(parentBuilder, fileBuilder.and())
    }

    @Test
    fun `then() returns the compilation builder`() {
      val parentBuilder = this@JavaCompilationBuilderTest.mockedBuilder()
      val fileBuilder = parentBuilder.JavaFileBuilder(mockk())
      assertSame(parentBuilder, fileBuilder.then())
    }
  }

  @Nested
  inner class CompanionTest {
    @Test
    fun `javac() delegates the system Java compiler to the compilation builder`() {
      val builder = JavaCompilationBuilder.javac()

      // Each call makes a new instance, unfortunately, so we can only test the type.
      val compilerType = ToolProvider.getSystemJavaCompiler()::class.java

      assertInstanceOf(compilerType, builder.compiler)
    }

    @Test
    fun `javac() initializes diagnostic listener with threadStackTraceProvider`() {
      val builder = JavaCompilationBuilder.javac()
      assertSame(
          StackTraceProvider.threadStackTraceProvider,
          builder.diagnosticListener.stackTraceProvider
      )
    }

    @Test
    fun `javac() wraps the compiler's StandardJavaFileManager in a JavaRamFileManager`() {
      val builder = JavaCompilationBuilder.javac()

      // Each call makes a new instance, unfortunately, so we can only test the type.
      val compiler = ToolProvider.getSystemJavaCompiler()
      val standardJavaFileManager = compiler.getStandardFileManager(
          mockk(),
          Locale.ROOT,
          StandardCharsets.UTF_8
      )
      val standardJavaFileManagerType = standardJavaFileManager::class.java

      assertInstanceOf(standardJavaFileManagerType, builder.fileManager.standardFileManager)
    }

    @Test
    fun `compiler() delegates the given compiler to the compilation builder`() {
      val compiler = mockk<JavaCompiler>()
      every { compiler.getStandardFileManager(any(), any(), any()) } returns mockk()
      val builder = JavaCompilationBuilder.compiler(compiler)

      assertSame(compiler, builder.compiler)
    }


    @Test
    fun `compiler() initializes diagnostic listener with threadStackTraceProvider`() {
      val compiler = mockk<JavaCompiler>()
      every { compiler.getStandardFileManager(any(), any(), any()) } returns mockk()
      val builder = JavaCompilationBuilder.compiler(compiler)

      assertSame(
          StackTraceProvider.threadStackTraceProvider,
          builder.diagnosticListener.stackTraceProvider
      )
    }

    @Test
    fun `compiler() wraps the compiler's StandardJavaFileManager in a JavaRamFileManager`() {
      val compiler = mockk<JavaCompiler>()
      val standardJavaFileManager = mockk<StandardJavaFileManager>()
      every { compiler.getStandardFileManager(any(), any(), any()) } returns standardJavaFileManager
      val builder = JavaCompilationBuilder.compiler(compiler)

      assertSame(standardJavaFileManager, builder.fileManager.standardFileManager)
    }
  }

  private fun mockedBuilder(
      compiler: JavaCompiler = mockk(),
      diagnosticListener: JavaDiagnosticListener = mockk(),
      fileManager: JavaRamFileManager = mockk()
  ) = JavaCompilationBuilder(compiler, diagnosticListener, fileManager)
}