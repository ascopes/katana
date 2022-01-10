package io.ascopes.katana.compilertesting.it

import io.ascopes.katana.compilertesting.Each
import io.ascopes.katana.compilertesting.assertions.assertThat
import io.ascopes.katana.compilertesting.compilation.JavaCompilationBuilder
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest

@Execution(ExecutionMode.CONCURRENT)
class SingleModuleCompilationITest {
  @Each.JavaVersion
  @ParameterizedTest
  fun `I can compile a basic 'Hello, World!' application`(version: SourceVersion) {
    //@formatter:off
    val compilation = JavaCompilationBuilder
        .javac()
        .releaseVersion(version)
        .sources()
            .createFile(
                fileName = "io/ascopes/helloworld/nonmodular/HelloWorld.java",
                """
                  package io.ascopes.helloworld.nonmodular;
                  
                  public class HelloWorld {
                    public static void main(String[] args) {
                      System.out.println("Hello, World!");
                    }
                  }
                """.trimIndent()
            )
            .and()
        .compile()

    assertThat(compilation)
        .isSuccessful()

    assertThat(compilation)
        .diagnostics()
        .hasNoWarnings()

    assertThat(compilation)
        .files()
        .generatedClassFile("io/ascopes/helloworld/nonmodular/HelloWorld.class")
    //@formatter:on
  }

  @Each.JavaVersion
  @ParameterizedTest
  fun `I can compile a basic 'Hello, World!' application with modules`(version: SourceVersion) {
    //@formatter:off
    val compilation = JavaCompilationBuilder
        .javac()
        .releaseVersion(version)
        .sources()
            .createFile(
                fileName = "io/ascopes/helloworld/modular/HelloWorld.java",
                """
                  package io.ascopes.helloworld.modular;
                  
                  public class HelloWorld {
                    public static void main(String[] args) {
                      System.out.println("Hello, World!");
                    }
                  }
                """.trimIndent()
            )
            .createFile(
                fileName = "module-info.java",
                """
                  module helloworld {
                    requires java.base;
                    exports io.ascopes.helloworld.modular;
                  }
                """.trimIndent()
            )
            .and()
        .compile()

      assertThat(compilation)
          .isSuccessful()

      assertThat(compilation)
          .diagnostics()
          .hasNoWarnings()

        assertThat(compilation)
            .files()
            .generatedClassFiles(
                "io/ascopes/helloworld/modular/HelloWorld.class",
                "module-info.class"
            )
    //@formatter:on
  }

  @Each.JavaVersion
  @ParameterizedTest
  fun `Annotation Processors get invoked on the given sources`(version: SourceVersion) {
    var invoked = false

    val annotationProcessor = object : AbstractProcessor() {
      override fun getSupportedSourceVersion() = version
      override fun getSupportedAnnotationTypes() = setOf("*")

      override fun process(
          annotations: MutableSet<out TypeElement>,
          roundEnv: RoundEnvironment
      ): Boolean {
        invoked = true
        return true
      }
    }

    //@formatter:off
    val compilation = JavaCompilationBuilder
        .javac()
        .releaseVersion(version)
        .sources()
            .createFile(
                fileName = "io/ascopes/helloworld/HelloWorld.java",
                """
                  package io.ascopes.helloworld;
                  
                  public class HelloWorld {
                    public static void main(String[] args) {
                      System.out.println("Hello, World!");
                    }
                  }
                """.trimIndent()
            )
            .and()
        .processors(annotationProcessor)
        .compile()

    assertThat(compilation)
        .isSuccessful()

    assertThat(compilation)
        .diagnostics()
        .hasNoWarnings()

    assertThat(compilation)
        .files()
        .generatedClassFile("io/ascopes/helloworld/HelloWorld.class")

    assertTrue(invoked, "annotation processor was not invoked")
    //@formatter:on
  }

  @Each.JavaVersion
  @ParameterizedTest
  fun `Headers get created for the given sources`(version: SourceVersion) {
    //@formatter:off
    val compilation = JavaCompilationBuilder
        .javac()
        .releaseVersion(version)
        .sources()
            .createFile(
                fileName = "io/ascopes/helloworld/HelloWorld.java",
                """
                  package io.ascopes.helloworld;
                  
                  public class HelloWorld {
                    public static void main(String[] args) {
                      System.out.println(createGreeting());
                    }
                    
                    private static native String createGreeting();
                  }
                """.trimIndent()
            )
            .and()
        .generateHeaders()
        .compile()

    assertThat(compilation)
        .isSuccessful()

    assertThat(compilation)
        .diagnostics()
        .hasNoWarnings()

    assertThat(compilation)
        .files()
        .generatedClassFile("io/ascopes/helloworld/HelloWorld.class")
        .generatedHeaderFile("io_ascopes_helloworld_HelloWorld.h")
    //@formatter:on
  }
}