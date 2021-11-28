package io.ascopes.katana.annotations;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import org.junit.jupiter.api.Test;

class MutableModelIntegrationTest {

  @Test
  void MutableModel_can_be_applied_to_interface_without_explicit_settings() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import java.util.SortedSet;",
            "",
            "import io.ascopes.katana.annotations.MutableModel;",
            "",
            "@MutableModel",
            "public interface User {",
            "  String getPrincipal();",
            "  String getCredential();",
            "  SortedSet<String> getAuthorities();",
            "}"
        ));

    assertThat(result).succeededWithoutWarnings();
  }

  @Test
  void MutableModel_can_be_applied_to_interface_with_explicit_settings() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import java.util.SortedSet;",
            "",
            "import io.ascopes.katana.annotations.MutableModel;",
            "import io.ascopes.katana.annotations.Settings;",
            "",
            "@MutableModel(@Settings)",
            "public interface User {",
            "  String getPrincipal();",
            "  String getCredential();",
            "  SortedSet<String> getAuthorities();",
            "}"
        ));

    assertThat(result).succeededWithoutWarnings();
  }

  @Test
  void MutableModel_is_not_repeatable() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import java.util.SortedSet;",
            "",
            "import io.ascopes.katana.annotations.MutableModel;",
            "",
            "@MutableModel",
            "@MutableModel",
            "public interface User {",
            "  String getPrincipal();",
            "  String getCredential();",
            "  SortedSet<String> getAuthorities();",
            "}"
        ));

    assertThat(result)
        .failed();
    assertThat(result)
        .hadErrorCount(1);
    assertThat(result)
        .hadErrorContainingMatch("not (a )?repeat");
  }

  @Test
  void MutableModel_can_be_applied_to_package() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.package-info",
            "",
            "@MutableModel",
            "package com.somecompany.userapi.models;",
            "",
            "import io.ascopes.katana.annotations.MutableModel;"
        ));

    assertThat(result)
        .succeededWithoutWarnings();
  }

  @Test
  void MutableModel_cannot_be_applied_to_constructor() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import io.ascopes.katana.annotations.MutableModel;",
            "",
            "public class User {",
            "  @MutableModel",
            "  public User() {",
            "  }",
            "}"
        ));

    assertThat(result)
        .failed();
    assertThat(result)
        .hadErrorCount(1);
    assertThat(result)
        .hadErrorContaining("not applicable");
  }

  @Test
  void MutableModel_cannot_be_applied_to_method() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import io.ascopes.katana.annotations.MutableModel;",
            "",
            "public class User {",
            "  @MutableModel",
            "  public String getPrincipal() {",
            "    return \"Steve\";",
            "  }",
            "}"
        ));

    assertThat(result)
        .failed();
    assertThat(result)
        .hadErrorCount(1);
    assertThat(result)
        .hadErrorContaining("not applicable");
  }

  @Test
  void MutableModel_cannot_be_applied_to_field() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import io.ascopes.katana.annotations.MutableModel;",
            "",
            "public class User {",
            "  @MutableModel",
            "  private String principal;",
            "}"
        ));

    assertThat(result)
        .failed();
    assertThat(result)
        .hadErrorCount(1);
    assertThat(result)
        .hadErrorContaining("not applicable");
  }

  @Test
  void MutableModel_cannot_be_applied_to_parameter() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import io.ascopes.katana.annotations.MutableModel;",
            "",
            "public class User {",
            "  private String principal;",
            "",
            "  public void setPrincipal(@MutableModel String principal) {",
            "    this.principal = principal;",
            "  }",
            "}"
        ));

    assertThat(result)
        .failed();
    assertThat(result)
        .hadErrorCount(1);
    assertThat(result)
        .hadErrorContaining("not applicable");
  }

  @Test
  void MutableModel_cannot_be_applied_to_type_argument() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import java.util.Iterator;",
            "import java.util.SortedSet;",
            "",
            "import io.ascopes.katana.annotations.MutableModel;",
            "",
            "public class User implements Iterable<@MutableModel String> {",
            "  private SortedSet<String> authorities;",
            "",
            "  @Override",
            "  public Iterator<String> iterator() {",
            "    return this.authorities.iterator();",
            "  }",
            "}"
        ));

    assertThat(result)
        .failed();
    assertThat(result)
        .hadErrorCount(1);
    assertThat(result)
        .hadErrorContaining("not applicable");
  }

  @Test
  void MutableModel_cannot_be_applied_to_type_use() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import io.ascopes.katana.annotations.MutableModel;",
            "",
            "public class User {",
            "",
            "  @Override",
            "  public String toString() {",
            "    @MutableModel",
            "    StringBuilder sb = new StringBuilder();",
            "",
            "    sb.append(\"User{}\");",
            "",
            "    return sb.toString();",
            "  }",
            "}"
        ));

    assertThat(result)
        .failed();
    assertThat(result)
        .hadErrorCount(1);
    assertThat(result)
        .hadErrorContaining("not applicable");
  }
}
