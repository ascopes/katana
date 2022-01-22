package io.ascopes.katana.compilertesting.java;

import static io.ascopes.katana.compilertesting.java.JavaAssertions.assertThatJavaCompilation;

import javax.lang.model.SourceVersion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * Basic integration tests, but implemented in Java rather than Kotlin as a proof that the Kotlin
 * classes can be utilised by Java test packs.
 */
class JavaSingleModuleCompilationITest {

  @DisplayName("I can compile a 'Hello, World' application in a Java test case")
  @Each.JavaVersion
  @ParameterizedTest
  void I_can_compile_a_hello_world_application(SourceVersion version) {
    //@formatter:off
    var compilation = JavaCompilationBuilder
        .javac()
        .releaseVersion(version)
        .sources()
            .create(
                "test/java/it/HelloWorld.java",
                "package test.java.it;",
                "",
                "public class HelloWorld {",
                "  public static void main(String[] args) {",
                "    System.out.println(\"Hello, World!\");",
                "  }",
                "}"
            )
            .and()
        .compile();

    assertThatJavaCompilation(compilation)
        .isSuccessful()
        .diagnostics()
        .hasNoWarnings();

    assertThatJavaCompilation(compilation)
        .files()
        .hasClassOutput("test/java/it/HelloWorld.class");
    //@formatter:off
  }
}
