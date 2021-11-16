package io.ascopes.katana.annotations;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import org.junit.jupiter.api.Test;

class ImmutableModelTest {

  @Test
  void ImmutableModel_can_be_applied_to_interface_without_explicit_settings() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import java.util.SortedSet;",
            "",
            "import io.ascopes.katana.annotations.ImmutableModel;",
            "",
            "@ImmutableModel",
            "public interface User {",
            "  String getPrincipal();",
            "  String getCredential();",
            "  SortedSet<String> getAuthorities();",
            "}"
        ));

    assertThat(result).succeededWithoutWarnings();
  }

  @Test
  void ImmutableModel_can_be_applied_to_interface_with_explicit_settings() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import java.util.SortedSet;",
            "",
            "import io.ascopes.katana.annotations.ImmutableModel;",
            "import io.ascopes.katana.annotations.Settings;",
            "",
            "@ImmutableModel(@Settings)",
            "public interface User {",
            "  String getPrincipal();",
            "  String getCredential();",
            "  SortedSet<String> getAuthorities();",
            "}"
        ));

    assertThat(result).succeededWithoutWarnings();
  }

  @Test
  void ImmutableModel_is_not_repeatable() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import java.util.SortedSet;",
            "",
            "import io.ascopes.katana.annotations.ImmutableModel;",
            "",
            "@ImmutableModel",
            "@ImmutableModel",
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
  void ImmutableModel_can_be_applied_to_package() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.package-info",
            "",
            "@ImmutableModel",
            "package com.somecompany.userapi.models;",
            "",
            "import io.ascopes.katana.annotations.ImmutableModel;"
        ));

    assertThat(result)
        .succeededWithoutWarnings();
  }

  @Test
  void ImmutableModel_cannot_be_applied_to_constructor() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import io.ascopes.katana.annotations.ImmutableModel;",
            "",
            "public class User {",
            "  @ImmutableModel",
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
  void ImmutableModel_cannot_be_applied_to_method() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import io.ascopes.katana.annotations.ImmutableModel;",
            "",
            "public class User {",
            "  @ImmutableModel",
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
  void ImmutableModel_cannot_be_applied_to_field() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import io.ascopes.katana.annotations.ImmutableModel;",
            "",
            "public class User {",
            "  @ImmutableModel",
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
  void ImmutableModel_cannot_be_applied_to_parameter() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import io.ascopes.katana.annotations.ImmutableModel;",
            "",
            "public class User {",
            "  private String principal;",
            "",
            "  public void setPrincipal(@ImmutableModel String principal) {",
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
  void ImmutableModel_cannot_be_applied_to_type_argument() {
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
            "import io.ascopes.katana.annotations.ImmutableModel;",
            "",
            "public class User implements Iterable<@ImmutableModel String> {",
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
  void ImmutableModel_cannot_be_applied_to_type_use() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import io.ascopes.katana.annotations.ImmutableModel;",
            "",
            "public class User {",
            "",
            "  @Override",
            "  public String toString() {",
            "    @ImmutableModel",
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
