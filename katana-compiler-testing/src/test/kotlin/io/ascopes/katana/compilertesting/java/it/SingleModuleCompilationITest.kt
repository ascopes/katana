package io.ascopes.katana.compilertesting.java.it

import io.ascopes.katana.compilertesting.java.Each
import io.ascopes.katana.compilertesting.java.JavaCompilationBuilder
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@Execution(ExecutionMode.CONCURRENT)
class SingleModuleCompilationITest {
  @Each.JavaVersion
  @ParameterizedTest
  fun `I can compile a basic 'Hello, World!' application`(version: SourceVersion) {
    JavaCompilationBuilder.javac()
        .releaseVersion(version)
        .sources {
          createFile("io/ascopes/helloworld/nonmodular/HelloWorld.java") {
            """
              package io.ascopes.helloworld.nonmodular;
              
              public class HelloWorld {
                public static void main(String[] args) {
                  System.out.println("Hello, World!");
                }
              }
            """.trimIndent()
          }
        }
        .compile()
        .succeededWithoutWarnings()
        .generatedClassFile("io/ascopes/helloworld/nonmodular/HelloWorld.class")
  }

  @Each.JavaVersion
  @ParameterizedTest
  fun `I can compile a basic 'Hello, World!' application with modules`(version: SourceVersion) {
    JavaCompilationBuilder.javac()
        .releaseVersion(version)
        .sources {
          createFile("io/ascopes/helloworld/modular/HelloWorld.java") {
            """
              package io.ascopes.helloworld.modular;
              
              public class HelloWorld {
                public static void main(String[] args) {
                  System.out.println("Hello, World!");
                }
              }
            """.trimIndent()
          }
          createFile("module-info.java") {
            """
              module helloworld {
                requires java.base;
                exports io.ascopes.helloworld.modular;
              }
            """.trimIndent()
          }
        }
        .compile()
        .succeededWithoutWarnings()
        .generatedClassFile("io/ascopes/helloworld/modular/HelloWorld.class")
        .generatedClassFile("module-info.class")
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

    JavaCompilationBuilder.javac()
        .releaseVersion(version)
        .sources {
          createFile("io/ascopes/helloworld/HelloWorld.java") {
            """
              package io.ascopes.helloworld;
              
              public class HelloWorld {
                public static void main(String[] args) {
                  System.out.println("Hello, World!");
                }
              }
            """.trimIndent()
          }
        }
        .processors(annotationProcessor)
        .compile()
        .succeededWithoutWarnings()
        .generatedClassFile("io/ascopes/helloworld/HelloWorld.class")

    assertTrue(invoked, "annotation processor was not invoked")
  }

  @Each.JavaVersion
  @ParameterizedTest
  fun `Headers get created for the given sources`(version: SourceVersion) {
    JavaCompilationBuilder.javac()
        .releaseVersion(version)
        .sources {
          createFile("io/ascopes/helloworld/HelloWorld.java") {
            """
              package io.ascopes.helloworld;
              
              public class HelloWorld {
                public static void main(String[] args) {
                  System.out.println(createGreeting());
                }
                
                private static native String createGreeting();
              }
            """.trimIndent()
          }
        }
        .generateHeaders()
        .compile()
        .succeededWithoutWarnings()
        .generatedClassFile("io/ascopes/helloworld/HelloWorld.class")
        .generatedHeaderFile("io_ascopes_helloworld_HelloWorld.h")
  }
}