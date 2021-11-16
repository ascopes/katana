package io.ascopes.katana.annotations;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

class SettersExcludeTest {

  @Test
  void Setters_Exclude_is_not_repeatable() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import java.util.SortedSet;",
            "",
            "import io.ascopes.katana.annotations.Setters;",
            "",
            "@Setters.Exclude",
            "@Setters.Exclude",
            "public interface User {",
            "  String getPrincipal();",
            "  String getCredential();",
            "  SortedSet<String> getAuthorities();",
            "}"
        ));

    assertThat(result)
        .failed();
    assertThat(result)
        .hadErrorContainingMatch("not (a )?repeat");
  }

  @Test
  void Setters_Exclude_cannot_be_applied_to_type() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import java.util.SortedSet;",
            "",
            "import io.ascopes.katana.annotations.Setters;",
            "",
            "@Setters.Exclude",
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
        .hadErrorContaining("not applicable");
  }

  @Test
  void Setters_Exclude_cannot_be_applied_to_annotation_type() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.Foo",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import java.util.SortedSet;",
            "",
            "import io.ascopes.katana.annotations.Setters;",
            "",
            "@Setters.Exclude",
            "public @interface Foo {",
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
  void Setters_Exclude_cannot_be_applied_to_package() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.package-info",
            "",
            "@Setters.Exclude",
            "package com.somecompany.userapi.models;",
            "",
            "import io.ascopes.katana.annotations.Setters;"
        ));

    assertThat(result)
        .failed();
    assertThat(result)
        .hadErrorCount(1);
    assertThat(result)
        .hadErrorContaining("not applicable");
  }

  @Test
  void Setters_Exclude_cannot_be_applied_to_constructor() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import io.ascopes.katana.annotations.Setters;",
            "",
            "public class User {",
            "  @Setters.Exclude",
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
  void Setters_Exclude_can_be_applied_to_method() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import io.ascopes.katana.annotations.Setters;",
            "",
            "public class User {",
            "  @Setters.Exclude",
            "  public String getPrincipal() {",
            "    return \"Steve\";",
            "  }",
            "}"
        ));

    assertThat(result).succeeded();
  }

  @Test
  void Setters_Exclude_cannot_be_applied_to_field() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import io.ascopes.katana.annotations.Setters;",
            "",
            "public class User {",
            "  @Setters.Exclude",
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
  void Setters_Exclude_cannot_be_applied_to_parameter() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import io.ascopes.katana.annotations.Setters;",
            "",
            "public class User {",
            "  private String principal;",
            "",
            "  public void setPrincipal(@Setters.Exclude String principal) {",
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
  void Setters_Exclude_cannot_be_applied_to_type_argument() {
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
            "import io.ascopes.katana.annotations.Setters;",
            "",
            "public class User implements Iterable<@Setters.Exclude String> {",
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
  void Setters_Exclude_cannot_be_applied_to_type_use() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import io.ascopes.katana.annotations.Setters;",
            "",
            "public class User {",
            "",
            "  @Override",
            "  public String toString() {",
            "    @Setters.Exclude",
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

  @EnabledForJreRange(min = JRE.JAVA_16)
  @Test
  void Setters_Exclude_cannot_be_applied_to_record_type() {
    Compilation result = Compiler
        .javac()
        .compile(forSourceLines(
            "com.somecompany.userapi.models.User",
            "",
            "package com.somecompany.userapi.models;",
            "",
            "import java.util.SortedSet;",
            "",
            "import io.ascopes.katana.annotations.Setters;",
            "",
            "@Setters.Exclude",
            "public record User(String principal, String credential, SortedSet<String> authorities) {",
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
