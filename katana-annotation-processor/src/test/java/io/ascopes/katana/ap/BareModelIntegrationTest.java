package io.ascopes.katana.ap;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import io.ascopes.katana.ap.logging.LoggerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;

class BareModelIntegrationTest {

  KatanaProcessor processor;

  @BeforeEach
  void setUp() {
    this.processor = new KatanaProcessor();
    LoggerFactory.globalLevel("TRACE");
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
                "import io.ascopes.katana.annotations.BuilderInitCheck;",
                "import io.ascopes.katana.annotations.Settings;",
                "import io.ascopes.katana.annotations.ToString;",
                "import io.ascopes.katana.annotations.ImmutableModel;",
                "import java.util.concurrent.atomic.AtomicBoolean;",
                "",
                "@MutableModel",
                "@ImmutableModel",
                "@Settings(builder = true, builderInitCheck = BuilderInitCheck.TYPESAFE)",
                "public interface BareModel {",
                "  String getFoo();",
                "  int getBar();",
                "  AtomicBoolean isBaz();",
                "  boolean isBork();",
                "  Boolean isQux();",
                "  @Deprecated",
                "  boolean getQuxx();",
                "  ",
                "  @ToString.CustomToString",
                "  static String asString(BareModel model) {",
                "    return \"hello world!\";",
                "  }",
                "  ",
                "}"
            )
        );

    assertThat(result)
        .succeededWithoutWarnings();
  }
}
