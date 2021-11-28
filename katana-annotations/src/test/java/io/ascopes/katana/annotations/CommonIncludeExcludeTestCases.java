package io.ascopes.katana.annotations;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import io.ascopes.katana.annotations.internal.ExclusionAdvice;
import io.ascopes.katana.annotations.internal.InclusionAdvice;
import java.lang.annotation.Annotation;
import javax.tools.JavaFileObject;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
abstract class CommonIncludeExcludeTestCases<A extends Annotation> extends TypeAware<A> {

  private final Class<A> annotation;
  private final Logger logger;

  CommonIncludeExcludeTestCases() {
    this.annotation = this.getGenericType();
    this.logger = LoggerFactory.getLogger(this.getClass());
  }

  @Test
  void is_inclusion_or_exclusion_advice_on_parent_class() {
    this.verifyAdviceIsThisAnnotation(
        this.annotation.getSimpleName().equalsIgnoreCase("include")
            ? InclusionAdvice.class
            : ExclusionAdvice.class
    );
  }

  @Test
  void not_repeatable() {
    Compilation compilation = this.compile(
        "RepeatableTestDummy",
        "",
        "package ##packageName##;",
        "",
        "class ##className## {",
        "",
        "  ##annotation##",
        "  ##annotation##",
        "  void foo() { }",
        "}"
    );

    CompilationSubject
        .assertThat(compilation)
        .failed();

    CompilationSubject
        .assertThat(compilation)
        .hadErrorContainingMatch("not (a )?repeat");
  }

  @Test
  void not_applicable_to_type() {
    Compilation compilation = this.compile(
        "NotApplicableToTypeTestDummy",
        "",
        "package ##packageName##;",
        "",
        "##annotation##",
        "class ##className## {",
        "",
        "  void foo() { }",
        "}"
    );

    CompilationSubject
        .assertThat(compilation)
        .failed();
    CompilationSubject
        .assertThat(compilation)
        .hadErrorCount(1);
    CompilationSubject
        .assertThat(compilation)
        .hadErrorContaining("not applicable");
  }

  @Test
  void not_applicable_to_annotation_type() {
    Compilation compilation = this.compile(
        "NotApplicableToAnnotationTypeTestDummy",
        "",
        "package ##packageName##;",
        "",
        "##annotation##",
        "@interface ##className## {",
        "}"
    );

    CompilationSubject
        .assertThat(compilation)
        .failed();
    CompilationSubject
        .assertThat(compilation)
        .hadErrorCount(1);
    CompilationSubject
        .assertThat(compilation)
        .hadErrorContaining("not applicable");
  }

  @Test
  void not_applicable_to_constructor() {
    Compilation compilation = this.compile(
        "NotApplicableToConstructorTestDummy",
        "",
        "package ##packageName##;",
        "",
        "class ##className## {",
        "",
        "  ##annotation##",
        "  public ##className##() { }",
        "}"
    );

    CompilationSubject
        .assertThat(compilation)
        .failed();
    CompilationSubject
        .assertThat(compilation)
        .hadErrorCount(1);
    CompilationSubject
        .assertThat(compilation)
        .hadErrorContaining("not applicable");
  }

  @Test
  void not_applicable_to_field() {
    Compilation compilation = this.compile(
        "NotApplicableToFieldTestDummy",
        "",
        "package ##packageName##;",
        "",
        "class ##className## {",
        "",
        "  ##annotation##",
        "  int i = 12;",
        "}"
    );

    CompilationSubject
        .assertThat(compilation)
        .failed();
    CompilationSubject
        .assertThat(compilation)
        .hadErrorCount(1);
    CompilationSubject
        .assertThat(compilation)
        .hadErrorContaining("not applicable");
  }

  @Test
  void not_applicable_to_parameter() {
    Compilation compilation = this.compile(
        "NotApplicableToParameterTestDummy",
        "",
        "package ##packageName##;",
        "",
        "class ##className## {",
        "",
        "  void foo(##annotation## int i) { }",
        "}"
    );

    CompilationSubject
        .assertThat(compilation)
        .failed();
    CompilationSubject
        .assertThat(compilation)
        .hadErrorCount(1);
    CompilationSubject
        .assertThat(compilation)
        .hadErrorContaining("not applicable");
  }

  @Test
  void not_applicable_to_type_parameter() {
    Compilation compilation = this.compile(
        "NotApplicableToTypeParameterTestDummy",
        "",
        "package ##packageName##;",
        "",
        "import java.util.Arrays;",
        "import java.util.Iterator;",
        "",
        "class ##className##",
        "    implements Iterable<##annotation## String>",
        "{",
        "  @Override",
        "  public Iterator<String> iterator() {",
        "    return Arrays.asList(\"foo\").iterator();",
        "  }",
        "}"
    );

    CompilationSubject
        .assertThat(compilation)
        .failed();
    CompilationSubject
        .assertThat(compilation)
        .hadErrorCount(1);
    CompilationSubject
        .assertThat(compilation)
        .hadErrorContaining("not applicable");
  }


  @Test
  void not_applicable_to_type_use() {
    Compilation compilation = this.compile(
        "NotApplicableToTypeUseTestDummy",
        "",
        "package ##packageName##;",
        "",
        "class ##className## {",
        "",
        "  void foo() {",
        "    ##annotation##",
        "    int i = 0;",
        "  }",
        "}"
    );

    CompilationSubject
        .assertThat(compilation)
        .failed();
    CompilationSubject
        .assertThat(compilation)
        .hadErrorCount(1);
    CompilationSubject
        .assertThat(compilation)
        .hadErrorContaining("not applicable");
  }

  @Test
  void not_applicable_to_package() {
    Compilation compilation = this.compile(
        "package-info",
        "",
        "##annotation##",
        "package ##packageName##;"
    );

    CompilationSubject
        .assertThat(compilation)
        .failed();
    CompilationSubject
        .assertThat(compilation)
        .hadErrorCount(1);
    CompilationSubject
        .assertThat(compilation)
        .hadErrorContaining("not applicable");
  }

  @EnabledForJreRange(min = JRE.JAVA_16)
  @Test
  void not_applicable_to_record_type() {
    Compilation compilation = this.compile(
        "NotApplicableToRecordTypeDummy",
        "",
        "package ##packageName##;",
        "",
        "##annotation##",
        "record ##className## (",
        "  String foo",
        ") { }"
    );

    CompilationSubject
        .assertThat(compilation)
        .failed();
    CompilationSubject
        .assertThat(compilation)
        .hadErrorCount(1);
    CompilationSubject
        .assertThat(compilation)
        .hadErrorContaining("not applicable");
  }

  @Test
  void applicable_to_method() {
    Compilation compilation = this.compile(
        "ApplicableToMethodTestDummy",
        "",
        "package ##packageName##;",
        "",
        "class ##className## {",
        "",
        "  ##annotation##",
        "  void foo() { }",
        "}"
    );

    CompilationSubject
        .assertThat(compilation)
        .succeededWithoutWarnings();
  }

  Compilation compile(String className, String... sourceLines) {
    String packageName = "testing." + this.annotation.getCanonicalName();
    String fileName = packageName + "." + className;

    for (int i = 0; i < sourceLines.length; ++i) {
      sourceLines[i] = sourceLines[i]
          .replace("##packageName##", packageName)
          .replace("##annotation##", "@" + this.annotation.getCanonicalName())
          .replace("##className##", className);
    }

    StackTraceElement calleeFrame = Thread.currentThread().getStackTrace()[2];

    if (this.logger.isDebugEnabled()) {
      this.logger.debug(
          "{}.{}:{} using test content:\n{}",
          calleeFrame.getFileName(),
          calleeFrame.getLineNumber(),
          calleeFrame.getMethodName(),
          String.join("\n", sourceLines)
      );
    }

    JavaFileObject file = JavaFileObjects.forSourceLines(fileName, sourceLines);

    return Compiler
        .javac()
        .compile(file);
  }

  @SuppressWarnings("unchecked")
  void verifyAdviceIsThisAnnotation(Class<? extends Annotation> adviceTag) {
    Annotation advice = this.annotation.getDeclaringClass().getAnnotation(adviceTag);

    BDDAssertions
        .assertThat(advice)
        .withFailMessage(
            "%s was not marked as %s on declaring class %s",
            this.annotation.getSimpleName(),
            adviceTag.getSimpleName(),
            this.annotation.getDeclaringClass().getCanonicalName()
        )
        .isNotNull();

    Class<? extends Annotation> actualValue;
    try {
      actualValue = (Class<? extends Annotation>) advice
          .annotationType()
          .getDeclaredMethod("value")
          .invoke(advice);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

    BDDAssertions
        .assertThat(actualValue)
        .withFailMessage(
            "%s was not the %s type for %s, %s was!",
            this.annotation.getSimpleName(),
            adviceTag.getSimpleName(),
            this.annotation.getDeclaringClass(),
            actualValue.getCanonicalName()
        )
        .isEqualTo(this.annotation);
  }
}
