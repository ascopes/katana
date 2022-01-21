package io.ascopes.katana.spi.annotationprocessor

import io.ascopes.katana.compilertesting.assertThat
import io.ascopes.katana.compilertesting.java.JavaCompilationBuilder
import org.junit.jupiter.params.ParameterizedTest
import javax.lang.model.SourceVersion

class KatanaSpiAnnotationProcessorTest {
  @Each.JavaVersion
  @ParameterizedTest
  fun `I can create a service file for a single legacy implementation`(version: SourceVersion) {
    //@formatter:off
    val compilation = JavaCompilationBuilder
        .javac()
        .releaseVersion(version)
        .sources()
          .create(
              "foo/bar/baz/Bork.java",
              """
                package foo.bar.baz;
                
                public interface Bork {
                  void doBork();
                }
              """.trimIndent()
          )
          .create(
              "foo/bar/baz/BorkImpl.java",
              """
                package foo.bar.baz;
                
                import io.ascopes.katana.spi.annotations.ServiceProvider;
                
                @ServiceProvider(Bork.class)
                public class BorkImpl implements Bork {
                  @Override
                  public void doBork() {
                    System.out.println("Bork");
                  }
                }
              """.trimIndent()
          )
          .and()
        .processors(KatanaSpiAnnotationProcessor())
        .compile()

    assertThat(compilation)
        .isSuccessfulWithoutWarnings()
        .ranInLegacyMode()
        .files()
        .hasClassOutput("META-INF/services/foo.bar.baz.Bork")
        .hasContent("foo.bar.baz.BorkImpl\n")
    //@formatter:on
  }

}