package io.ascopes.katana.compilertesting.java

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.charset.StandardCharsets
import java.util.Locale
import javax.annotation.processing.Processor
import javax.lang.model.SourceVersion
import javax.tools.JavaCompiler
import javax.tools.StandardJavaFileManager
import javax.tools.ToolProvider

class JavaCompilationBuilderTest {
  @ParameterizedTest
  @ValueSource(ints = [11, 12, 13, 14, 15, 16, 17, 18, 19, 20])
  fun `sourceVersion(int) sets the source version flag`(version: Int) {
    val builder = this.builderWithMocks()
    builder.options += listOf("foo", "bar", "baz")

    builder.sourceVersion(version)

    assertEquals(listOf("foo", "bar", "baz", JavaCompilationBuilder.SOURCE_FLAG, "$version"), builder.options)
  }

  @ParameterizedTest
  @Each.JavaVersion
  fun `sourceVersion(SourceVersion) sets the source version flag`(version: SourceVersion) {
    val builder = this.builderWithMocks()
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
    val builder = this.builderWithMocks()
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
    val builder = this.builderWithMocks()
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
    val builder = this.builderWithMocks()
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
    val builder = this.builderWithMocks()
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
    val builder = this.builderWithMocks()
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
    val builder = this.builderWithMocks()
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
    val builder = this.builderWithMocks()
    builder.options += listOf("drum", "n", "bass")

    builder.options("--foo", "--bar", "--baz 12")

    assertEquals(
        listOf("drum", "n", "bass", "--foo", "--bar", "--baz 12"),
        builder.options
    )
  }

  @Test
  fun `processors() appends to the list of processors`() {
    val builder = this.builderWithMocks()
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
    val builder = this.builderWithMocks()
    builder.options += listOf("some", "existing", "stuff", "here")

    builder.treatWarningsAsErrors()

    assertEquals(
        listOf("some", "existing", "stuff", "here", JavaCompilationBuilder.WERROR_FLAG),
        builder.options
    )
  }

  private fun builderWithMocks(
      compiler: JavaCompiler = mockk(),
      diagnosticListener: JavaDiagnosticListener = mockk(),
      fileManager: JavaRamFileManager = mockk()
  ) = JavaCompilationBuilder(compiler, diagnosticListener, fileManager)

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
          JavaStackTraceProvider.threadStackTraceProvider,
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
          JavaStackTraceProvider.threadStackTraceProvider,
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
}