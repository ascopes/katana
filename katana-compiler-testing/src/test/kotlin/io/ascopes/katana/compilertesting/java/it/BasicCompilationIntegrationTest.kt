package io.ascopes.katana.compilertesting.java.it

import io.ascopes.katana.compilertesting.java.JavaCompiler
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Order
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

class BasicCompilationIntegrationTest {
  @MethodSource("allSupportedJavaVersions")
  @ParameterizedTest
  @Order(0)
  fun `I can compile a simple hello world application`(version: SourceVersion) {
    Companion
        .unnamedModuleCompilation(version)
        .compile()
        .succeededWithoutWarnings()
        .generatedClassFile("my/packagename/here/HelloWorld.class")
  }

  @MethodSource("allSupportedJavaVersions")
  @ParameterizedTest
  @Order(1)
  fun `I can compile multiple modules in one run`(version: SourceVersion) {
    Companion
        .multiModuleCompilation(version)
        .compile()
        .succeededWithoutWarnings()
        .generatedClassFile("test.greeter/my/packagename/here/greeter/GreeterFactory.class")
        .generatedClassFile("test.greeter/module-info.class")
        .generatedClassFile("test.helloworld/my/packagename/here/helloworld/Main.class")
        .generatedClassFile("test.helloworld/module-info.class")
  }

  @MethodSource("allSupportedJavaVersions")
  @ParameterizedTest
  @Order(2)
  fun `I can specify annotation processors to run during compilation for standard compilations`(
      version: SourceVersion
  ) {
    var wasCalled = false

    val processor = object : AbstractProcessor() {
      override fun getSupportedSourceVersion() = version
      override fun getSupportedAnnotationTypes() = setOf("*")

      override fun process(
          annotations: MutableSet<out TypeElement>,
          roundEnv: RoundEnvironment
      ): Boolean {
        // This gets called once to process, and once after processing is over.
        wasCalled = true
        return true
      }
    }

    Companion
        .unnamedModuleCompilation(version)
        .processors(processor)
        .compile()
        .succeededWithoutWarnings()

    assertTrue(wasCalled, "expected annotation processor to be invoked once")
  }

  @MethodSource("allSupportedJavaVersions")
  @ParameterizedTest
  @Order(3)
  fun `I can specify annotation processors to run during compilation for multimodule compilations`(
      version: SourceVersion
  ) {
    var wasCalled = false

    val processor = object : AbstractProcessor() {
      override fun getSupportedSourceVersion() = version
      override fun getSupportedAnnotationTypes() = setOf("*")

      override fun process(
          annotations: MutableSet<out TypeElement>,
          roundEnv: RoundEnvironment
      ): Boolean {
        // This gets called once to process, and once after processing is over.
        wasCalled = true
        return true
      }
    }

    Companion
        .multiModuleCompilation(version)
        .processors(processor)
        .compile()
        .succeededWithoutWarnings()

    assertTrue(wasCalled, "expected annotation processor to be invoked once")
  }

  @MethodSource("allSupportedJavaVersions")
  @ParameterizedTest
  @Order(4)
  fun `I can request that headers get produced for native sources for standard compilations`(
      version: SourceVersion
  ) {
    JavaCompiler
        .javac()
        .releaseVersion(version)
        .generateHeaders()
        .sources {
          this.createFile(
              "my/packagehere/Epoll.java",
              """
              package my.packagehere;  
                
              public class Epoll {
                  public static native int openEpollFileDescriptor();
                  public static native void closeEpollFileDescriptor(int fd);
              }
              """.trimIndent()
          )
        }
        .compile()
        .succeededWithoutWarnings()
        .generatedClassFile("my/packagehere/Epoll.class")
        .generatedHeaderFile("my_packagehere_Epoll.h")
  }

  @MethodSource("allSupportedJavaVersions")
  @ParameterizedTest
  @Order(5)
  fun `I can request that headers get produced for native sources for multimodule compilations`(
      version: SourceVersion
  ) {
    JavaCompiler
        .javac()
        .releaseVersion(version)
        .generateHeaders()
        .moduleSources("my.packagehere") {
          this.createFile(
              "module-info.java",
              """
              module my.packagehere {
                requires java.base;
                exports my.packagehere;
              }
              """.trimIndent()
          )

          this.createFile(
              "my/packagehere/Epoll.java",
              """
              package my.packagehere;  
                
              public class Epoll {
                  public static native int openEpollFileDescriptor();
                  public static native void closeEpollFileDescriptor(int fd);
              }
              """.trimIndent()
          )
        }
        .compile()
        .succeededWithoutWarnings()
        .generatedClassFile("my.packagehere/my/packagehere/Epoll.class")
        .generatedHeaderFile("my.packagehere/my_packagehere_Epoll.h")
  }

  private companion object {
    @JvmStatic
    fun allSupportedJavaVersions() = SourceVersion
        .values()
        .filter { it >= SourceVersion.RELEASE_11 }

    private fun unnamedModuleCompilation(version: SourceVersion) = JavaCompiler.javac()
        .releaseVersion(version)
        .sources {
          this.createFile(
              "my/packagename/here/HelloWorld.java",
              """
              package my.packagename.here;
              
              public class HelloWorld {
                public static void main(String[] args) {
                  System.out.println("Hello, World!");
                }
              }
              """.trimIndent()
          )
        }

    private fun multiModuleCompilation(version: SourceVersion) = JavaCompiler.javac()
        .releaseVersion(version)
        .moduleSources("test.greeter") {
          this.createFile(
              "my/packagename/here/greeter/GreeterFactory.java",
              """
              package my.packagename.here.greeter;
              
              public class GreeterFactory {
                public String createGreeting(String name) {
                  return "Hello, " + name + "!";
                }
              }
              """.trimIndent()
          )
          this.createFile(
              "module-info.java",
              """
              module test.greeter {
                requires java.base;
                exports my.packagename.here.greeter;
              }
              """.trimIndent()
          )
        }
        .moduleSources("test.helloworld") {
          this.createFile(
              "my/packagename/here/helloworld/Main.java",
              """
              package my.packagename.here.helloworld;
              
              import my.packagename.here.greeter.GreeterFactory;
              
              public class Main {
                public static void main(String[] args) {
                  String greeting = new GreeterFactory().createGreeting("World");
                  System.out.println(greeting);
                }
              }
              """.trimIndent()
          )
          this.createFile(
              "module-info.java",
              """
              module test.helloworld {
                requires java.base;
                requires test.greeter;
                exports my.packagename.here.helloworld;
              }
              """.trimIndent()
          )
        }
  }
}