package io.ascopes.katana.ap;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;

class BareModelTest {
  @Test
  void test_bare_model() {
    Compilation result = Compiler
        .javac()
        .withProcessors(new KatanaAnnotationProcessor())
        .compile(forSourceLines(
            "test.BareModel",
            "package test;",
            "import io.ascopes.katana.annotations.MutableModel;",
            "import io.ascopes.katana.annotations.ImmutableModel;",
            "",
            "@MutableModel",
            "@ImmutableModel",
            "public interface BareModel {",
            "  String getFoo();",
            "  int getBar();",
            "}"
        ));

    assertThat(result)
        .succeededWithoutWarnings();
  }
}
