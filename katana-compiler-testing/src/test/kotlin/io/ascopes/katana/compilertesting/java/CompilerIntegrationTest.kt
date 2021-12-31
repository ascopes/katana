package io.ascopes.katana.compilertesting.java

import org.junit.jupiter.api.Test

class CompilerIntegrationTest {
  @Test
  fun `I can compile a hello world application`() {
    InMemoryCompiler
        .javac()
        .options("-g", "-Werror")
        .sourceAndTargetVersion(8)
        .file(
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
        .compile()
        .succeededWithoutWarnings()
        .generatedClassFile("my/packagename/HelloWorld.class")
  }
}