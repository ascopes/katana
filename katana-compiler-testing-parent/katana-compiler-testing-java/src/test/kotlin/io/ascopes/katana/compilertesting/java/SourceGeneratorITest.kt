package io.ascopes.katana.compilertesting.java

import io.ascopes.katana.compilertesting.java.JavaAssertions.assertThatJavaCompilation
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest

class SourceGeneratorITest {
  @Each.JavaVersion
  @ParameterizedTest
  fun `Annotation processor sources can be inspected`(version: SourceVersion) {
    val annotationType = InspectMe::class.java.canonicalName

    //@formatter:off
    val compilation = JavaCompilationBuilder
        .javac()
        .releaseVersion(version)
        .files(StandardLocation.SOURCE_PATH)
          .create(
              "foo/bar/baz/Bork.java",
              """
                package foo.bar.baz;
                
                @$annotationType
                public class Bork {
                }
              """.trimIndent()
          )
          .and()
        .processors(MyAnnotationProcessor(version))
        .compile()

    assertThatJavaCompilation(compilation)
        .isSuccessfulWithoutWarnings()

    assertThatJavaCompilation(compilation)
        .files()
        .hasClassOutput("foo.bar.baz.Bork/info.txt")
          .hasContent("Hello, World!")
          .textSatisfies {
            assertEquals("Hello, World!", it)
          }
    //@formatter:on
  }

  class MyAnnotationProcessor(private val version: SourceVersion) : AbstractProcessor() {
    override fun getSupportedSourceVersion() = version

    override fun getSupportedAnnotationTypes() = setOf(InspectMe::class.java.canonicalName)

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
      annotations.forEach { annotation ->
        roundEnv
            .getElementsAnnotatedWith(annotation)
            .filterIsInstance<TypeElement>()
            .forEach { type ->
              processingEnv.filer
                  .createResource(
                      StandardLocation.CLASS_OUTPUT,
                      type.qualifiedName,
                      "info.txt"
                  )
                  .openWriter()
                  .use { writer ->
                    writer.write("Hello, World!")
                  }
            }
      }

      return true
    }
  }

  annotation class InspectMe
}