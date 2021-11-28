package io.ascopes.katana.ap;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;

class BareModelTest {

  KatanaProcessor processor;

  @BeforeEach
  void setUp() {
    this.processor = new KatanaProcessor();
  }

  @Test
  void test_bare_model() {
    Compilation result = Compiler
        .javac()
        .withProcessors(this.processor)
        .compile(
            forSourceLines(
                "test.package-info",
                "@ImmutableModel",
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
                "  String getFoo();",
                "  int getBar();",
                "  AtomicBoolean isBaz();",
                "  boolean isBork();",
                "  Boolean isQux();",
                "  @Deprecated",
                "  boolean getQuxx();",
                "}"
            )
        );

    assertThat(result)
        .succeededWithoutWarnings();
  }
}