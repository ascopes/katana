package io.ascopes.katana.ap.codegen;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.ascopes.katana.annotations.Generated;
import io.ascopes.katana.annotations.Visibility;
import io.ascopes.katana.ap.codegen.init.InitTrackerFactory;
import io.ascopes.katana.ap.descriptors.Attribute;
import io.ascopes.katana.ap.descriptors.Model;
import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import io.ascopes.katana.ap.utils.Logger;
import io.ascopes.katana.ap.utils.NamingUtils;
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
  private final AnnotationSpec overrideAnnotation;
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

    this.overrideAnnotation = AnnotationSpec
        .builder(Override.class)
        .build();
  }

  /**
   * Generate a Java source file from a model.
   *
   * @param model the model to generate from.
   * @return the resultant file object.
   */
  public JavaFile buildJavaFileFrom(Model model) {
    this.logger.debug("Building Java file for {}", model);

    TypeSpec typeSpec = this.buildModelTypeSpecFrom(model);
    return this.wrapTypeSpecInPackage(typeSpec, model);
  }

  private JavaFile wrapTypeSpecInPackage(TypeSpec typeSpec, Model model) {
    return JavaFile
        .builder(model.getPackageName(), typeSpec)
        .skipJavaLangImports(true)
        .indent("    ")
        .build();
  }

  private TypeSpec buildModelTypeSpecFrom(Model model) {
    TypeSpec.Builder builder = TypeSpec
        .classBuilder(model.getClassName())
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addSuperinterface(model.getSuperInterface().asType())
        .addAnnotation(this.generatedAnnotation);

    model
        .getDeprecatedAnnotation()
        .map(AnnotationSpec::get)
        .ifPresent(builder::addAnnotation);

    this.applyAttributes(builder, model);

    return builder.build();
  }

  private void applyAttributes(TypeSpec.Builder typeSpecBuilder, Model model) {
    SettingsCollection settings = model.getSettingsCollection();

    for (Attribute attribute : model.getAttributes()) {
      typeSpecBuilder
          .addField(this.fieldFor(attribute))
          .addMethod(this.getterFor(attribute, settings));

      if (model.isMutable()) {
        typeSpecBuilder.addMethod(this.setterFor(attribute, settings));
      }
    }
  }

  private FieldSpec fieldFor(Attribute attribute) {
    FieldSpec.Builder builder = FieldSpec
        .builder(attribute.getType(), attribute.getIdentifier())
        .addModifiers(this.visibilityModifiers(attribute.getFieldVisibility()));

    if (attribute.isFinal()) {
      builder.addModifiers(Modifier.FINAL);
    }

    if (attribute.isTransient()) {
      builder.addModifiers(Modifier.TRANSIENT);
    }

    return builder.build();
  }

  private MethodSpec getterFor(Attribute attribute, SettingsCollection settings) {
    MethodSpec.Builder builder = MethodSpec
        .overriding(attribute.getGetterToOverride())
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addJavadoc("@return the value of the {@code $L} attribute.", attribute.getName())
        .addStatement("return this.$L", attribute.getIdentifier());

    attribute
        .getDeprecatedAnnotation()
        .map(AnnotationSpec::get)
        .ifPresent(builder::addAnnotation);

    return builder.build();
  }

  private MethodSpec setterFor(Attribute attribute, SettingsCollection settings) {
    String setterName = NamingUtils.addPrefixCamelCase(
        settings.getSetterPrefix().getValue(),
        attribute.getName()
    );

    MethodSpec.Builder builder = MethodSpec
        .methodBuilder(setterName)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addParameter(attribute.getType(), attribute.getIdentifier(), Modifier.FINAL)
        .addJavadoc(
            "@param $L the value to set for the {@code $L} attribute",
            attribute.getIdentifier(),
            attribute.getName()
        )
        .addStatement("this.$1L = $1L", attribute.getIdentifier());

    attribute
        .getDeprecatedAnnotation()
        .map(AnnotationSpec::get)
        .ifPresent(builder::addAnnotation);

    return builder.build();
  }

  private Modifier[] visibilityModifiers(Visibility visibility) {
    switch (visibility) {
      case PRIVATE:
        return new Modifier[]{Modifier.PRIVATE};
      case PROTECTED:
        return new Modifier[]{Modifier.PROTECTED};
      case PUBLIC:
        return new Modifier[]{Modifier.PUBLIC};
      case PACKAGE_PRIVATE:
      default:
        return new Modifier[]{};
    }
  }
}

