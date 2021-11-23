package io.ascopes.katana.ap;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.junit.jupiter.api.Test;

class BareModelTest {

  @Test
  void test_bare_model() {
    Compilation result = Compiler
        .javac()
        .withProcessors(new KatanaCodegenAnnotationProcessor())
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

    result.generatedFiles()
        .stream()
        .filter(file -> file.getKind() == Kind.SOURCE)
        .forEach(file -> System.err.printf(
            "=================== Generated %s ===================%n%s%n",
            file.getName(),
            readStream(file)
        ));
  }

  static String readStream(JavaFileObject file) {
    try {
      try (InputStream is = file.openInputStream()) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int c;

        while ((c = is.read(buff)) != -1) {
          baos.write(buff, 0, c);
        }

        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
      }
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}