package io.ascopes.katana.ap.codegen;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.ascopes.katana.annotations.Generated;
import io.ascopes.katana.ap.codegen.init.InitTrackerFactory;
import io.ascopes.katana.ap.descriptors.Model;
import io.ascopes.katana.ap.utils.Logger;
import io.ascopes.katana.ap.utils.Result;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import javax.lang.model.element.Modifier;

/**
 * Factory for generating Java classes from descriptor definitions.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class SourceFileFactory {

  private final Logger logger;
  private final AnnotationSpec generatedAnnotation;
  private final InitTrackerFactory initTrackerFactory;

  public SourceFileFactory() {
    this.logger = new Logger();
    this.initTrackerFactory = new InitTrackerFactory();

    OffsetDateTime now = OffsetDateTime.now(Clock.systemDefaultZone());
    String nowString = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(now);

    this.generatedAnnotation = AnnotationSpec
        .builder(Generated.class)
        .addMember("name", "$S", "Katana Annotation Processor")
        .addMember("date", "$S", nowString)
        .build();
  }

  /**
   * Generate a Java source file from a model.
   *
   * @param model the model to generate from.
   * @return the resultant file, or a failure if something went wrong.
   */
  public Result<JavaFile> buildJavaFileFrom(Model model) {
    this.logger.debug("Building Java file for {}", model);

    return this
        .buildModelTypeSpecFrom(model)
        .ifOkMap(typeSpec -> this.wrapTypeSpec(model, typeSpec));
  }

  private JavaFile wrapTypeSpec(Model model, TypeSpec typeSpec) {
    return JavaFile
        .builder(model.getPackageName(), typeSpec)
        .skipJavaLangImports(true)
        .indent("    ")
        .build();
  }

  private Result<TypeSpec> buildModelTypeSpecFrom(Model model) {
    TypeSpec.Builder typeSpec = TypeSpec
        .classBuilder(model.getClassName())
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addAnnotation(this.generatedAnnotation);

    return Result.ok(typeSpec.build());
  }
}
