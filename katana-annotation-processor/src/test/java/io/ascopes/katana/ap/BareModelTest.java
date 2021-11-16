package io.ascopes.katana.ap;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import org.junit.jupiter.api.Test;

class BareModelTest {

  @Test
  void test_bare_model() {
    Compilation result = Compiler
        .javac()
        .withProcessors(new KatanaAnnotationProcessor())
        .compile(
            forSourceLines(
                "test.package-info",
                "@ImmutableModel(@Settings(className=\"Unmodifiable*\"))",
                "package test;",
                "import io.ascopes.katana.annotations.ImmutableModel;",
                "import io.ascopes.katana.annotations.Settings;"
            ),
            forSourceLines(
                "test.BareModel",
                "package test;",
                "import io.ascopes.katana.annotations.MutableModel;",
                "import io.ascopes.katana.annotations.ImmutableModel;",
                "import java.util.concurrent.atomic.AtomicBoolean;",
                "",
                "@MutableModel",
                "@ImmutableModel",
                "public interface BareModel {",
                "  void getNothing();",
                "  String getFoo();",
                "  int getBar();",
                "  AtomicBoolean isBaz();",
                "  boolean isBork();",
                "  Boolean isQux();",
                "  boolean getQuxx();",
                "}"
            )
        );

    assertThat(result)
        .succeededWithoutWarnings();
  }
}