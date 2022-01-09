package io.ascopes.katana.compilertesting.it

import io.ascopes.katana.compilertesting.Each
import io.ascopes.katana.compilertesting.assertions.assertThat
import io.ascopes.katana.compilertesting.compilation.JavaCompilationBuilder
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@Execution(ExecutionMode.CONCURRENT)
class MultiModuleCompilationITest {
  @Each.JavaVersion
  @ParameterizedTest
  fun `I can compile a basic 'Hello, World!' application`(version: SourceVersion) {
    //@formatter:off
    val compilation = JavaCompilationBuilder
        .javac()
        .releaseVersion(version)
        .multiModuleSources("helloworld.greet")
            .createFile(
                "io/me/helloworld/greet/Greeter.java",
                """
                  package io.me.helloworld.greet;
                    
                  public class Greeter {
                    public String greet(String name) {
                      return "Hello, " + name + "!"; 
                    }
                  }
                """.trimIndent()
            )
            .createFile(
                "module-info.java",
                """
                  module helloworld.greet {
                    requires java.base;
                    exports io.me.helloworld.greet;
                  }
                """.trimIndent()
            )
            .and()
        .multiModuleSources("helloworld.main")
            .createFile(
                "io/me/helloworld/main/Main.java",
                """
                  package io.me.helloworld.main;
                  
                  import io.me.helloworld.greet.Greeter;
                  
                  public class Main {
                    public static void main(String[] args) {
                      var greeter = new Greeter();
                      var greeting = greeter.greet("World");
                      System.out.println(greeting);
                    }
                  }
                """.trimIndent()
            )
            .createFile(
                "module-info.java",
                """
                  module helloworld.main {
                    requires java.base;
                    requires helloworld.greet;
                    exports io.me.helloworld.main;
                  }
                """.trimIndent()
            )
            .and()
        .compile()

    assertThat(compilation)
        .isSuccessful()
        .diagnostics()
        .hasNoWarnings()

    assertThat(compilation)
        .files()
        .generatedModuleClassFiles(
            "helloworld.greet",
            "io/me/helloworld/greet/Greeter.class",
            "module-info.class"
        )
        .generatedModuleClassFiles(
            "helloworld.main",
            "io/me/helloworld/main/Main.class",
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
        .multiModuleSources("helloworld.greet")
            .createFile(
                "io/ascopes/helloworld/greet/Greeter.java",
                """
                  package io.ascopes.helloworld.greet;
                  
                  public class Greeter {
                    public String greet(String name) {
                      return "Hello, " + name + "!"; 
                    }
                  }
                """.trimIndent()
            )
            .createFile(
                "module-info.java",
                """
                module helloworld.greet {
                  requires java.base;
                  exports io.ascopes.helloworld.greet;
                }
              """.trimIndent()
            )
            .and()
        .multiModuleSources("helloworld.main")
            .createFile(
                "io/ascopes/helloworld/main/Main.java",
                """
                  package io.ascopes.helloworld.main;
                  
                  import io.ascopes.helloworld.greet.Greeter;
                  
                  public class Main {
                    public static void main(String[] args) {
                      var greeter = new Greeter();
                      var greeting = greeter.greet("World");
                      System.out.println(greeting);
                    }
                  }
                """.trimIndent()
            )
            .createFile(
                "module-info.java",
                """
                  module helloworld.main {
                    requires java.base;
                    requires helloworld.greet;
                    exports io.ascopes.helloworld.main;
                  }
                """.trimIndent()
            )
            .and()
        .processors(annotationProcessor)
        .compile()

    assertThat(compilation)
        .isSuccessful()
        .diagnostics()
        .hasNoWarnings()

    assertThat(compilation)
        .files()
        .generatedModuleClassFiles(
            "helloworld.greet",
            "io/ascopes/helloworld/greet/Greeter.class",
            "module-info.class"
        )
        .generatedModuleClassFiles(
            "helloworld.main",
            "io/ascopes/helloworld/main/Main.class",
            "module-info.class"
        )

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
        .multiModuleSources("helloworld.greet")
            .createFile(
                "io/me/helloworld/greet/Greeter.java",
                """
                  package io.me.helloworld.greet;
                  
                  public class Greeter {
                    public native String greet(String name);
                  }
                """.trimIndent()
            )
            .createFile(
                "module-info.java",
                """
                  module helloworld.greet {
                    requires java.base;
                    exports io.me.helloworld.greet;
                  }
                """.trimIndent()
            )
            .and()
        .multiModuleSources("helloworld.main")
            .createFile(
                "io/me/helloworld/main/Main.java",
                """
                  package io.me.helloworld.main;
                  
                  import io.me.helloworld.greet.Greeter;
                  
                  public class Main {
                    public static void main(String[] args) {
                      var greeter = new Greeter();
                      var greeting = greeter.greet("World");
                      System.out.println(greeting);
                    }
                  }
                """.trimIndent()
            )
            .createFile(
                "module-info.java",
                """
                  module helloworld.main {
                    requires java.base;
                    requires helloworld.greet;
                    exports io.me.helloworld.main;
                  }
                """.trimIndent()
            )
        .and()
        .generateHeaders()
        .compile()

    assertThat(compilation)
        .isSuccessful()
        .diagnostics()
        .hasNoWarnings()

    assertThat(compilation)
        .files()
        .generatedModuleClassFiles(
            moduleName = "helloworld.greet",
            "io/me/helloworld/greet/Greeter.class",
            "module-info.class"
        )
        .generatedModuleClassFiles(
            moduleName = "helloworld.main",
            "io/me/helloworld/main/Main.class",
            "module-info.class"
        )
        .generatedModuleHeaderFile(
            moduleName = "helloworld.greet",
            "io_me_helloworld_greet_Greeter.h"
        )
    //@formatter:on
  }
}