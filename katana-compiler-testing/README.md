# katana-compiler-testing

In-memory integrations for the java.compiler module to allow integration testing the compilation of
Java sources and the behaviour of annotation processors.

Inspired by [Google's Compile Testing API](https://github.com/google/compile-testing), but with a
focus on supporting idiomatic features of newer releases of the JDK, such as multi-module
compilation. All sources and outputs are kept in an in-memory file system
(specifically [Googles JIMFS](https://github.com/google/jimfs)) to ensure fast tests that are
lightweight, stateless, and reproducible.

This can be run on any Java version that is Java 11 or newer, but can back compile to any Java 
version within test cases.

----

## Basic usage

An example of usage may look like the following:

```java
import org.junit.jupiter.api.Test;

import static io.ascopes.katana.compilertesting.CompilerAssert.assertThatCompilation;
import static io.ascopes.katana.compilertesting.java.JavaCompilationBuilder.javac;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MyIntegrationTest {

    @Test
    void test_that_i_can_compile_a_file() {
        var compilation = javac()
                .releaseVersion(17)
                .sources()
                .create(
                        "module-info.java",
                        "module my.helloworld.app {",
                        "    requires java.base;",
                        "    exports my.packagename.here;",
                        "}"
                )
                .create(
                        "my/packagename/here/HelloWorld.java",
                        "package my.packagename.here;",
                        "",
                        "public class HelloWorld {",
                        "    public static void main(String[] args) {",
                        "        System.out.println(\"Hello, World!\");",
                        "    }",
                        "}"
                )
                .and()
                .compile();

        assertThatCompilation(compilation)
                .isSuccessfulWithoutWarnings()
                .satisfies(result -> {
                    assertEquals("", result.getLogs(), "Expected empty logs");
                });
                
        assertThatCompilation(compilation)
                .files()
                .hasClassOutputs("module-info.class", "my/packagename/here/HelloWorld.class");
    }
}
```