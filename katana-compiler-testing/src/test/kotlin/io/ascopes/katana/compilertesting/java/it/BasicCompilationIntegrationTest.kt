package io.ascopes.katana.compilertesting.java.it

import io.ascopes.katana.compilertesting.java.InMemoryCompiler
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import javax.lang.model.SourceVersion

class BasicCompilationIntegrationTest {
  @MethodSource("allSupportedJavaVersions")
  @ParameterizedTest
  fun `I can compile a simple hello world application`(version: SourceVersion) {
    InMemoryCompiler.javac()
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
        .compile()
        .succeededWithoutWarnings()
        .generatedClassFile("my/packagename/here/HelloWorld.class")
  }

  @MethodSource("allSupportedJavaVersions")
  @ParameterizedTest
  fun `I can compile multiple modules in one run`(version: SourceVersion) {
    InMemoryCompiler.javac()
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
        .compile()
        .succeededWithoutWarnings()
        .generatedClassFile("test.greeter/my/packagename/here/greeter/GreeterFactory.class")
        .generatedClassFile("test.greeter/module-info.class")
        .generatedClassFile("test.helloworld/my/packagename/here/helloworld/Main.class")
        .generatedClassFile("test.helloworld/module-info.class")
  }

  private companion object {
    @JvmStatic
    fun allSupportedJavaVersions() = SourceVersion
        .values()
        .filter { it >= SourceVersion.RELEASE_11 }
  }
}