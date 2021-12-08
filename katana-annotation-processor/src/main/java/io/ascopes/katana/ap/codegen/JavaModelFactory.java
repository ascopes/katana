package io.ascopes.katana.ap.codegen;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.ascopes.katana.ap.analysis.Attribute;
import io.ascopes.katana.ap.analysis.Model;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.utils.CodeGenUtils;
import javax.lang.model.element.Modifier;

/**
 * Factory for generating Java classes from descriptor definitions.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class JavaModelFactory {

  private final Logger logger;
  private final FieldFactory fieldFactory;
  private final GetterFactory getterFactory;
  private final SetterFactory setterFactory;
  private final ConstructorFactory constructorFactory;
  private final DelegatingBuilderFactory delegatingBuilderFactory;
  private final EqualsHashCodeFactory equalsHashCodeFactory;
  private final ToStringFactory toStringFactory;

  /**
   * Initialize this factory.
   */
  public JavaModelFactory() {
    this.logger = LoggerFactory.loggerFor(this.getClass());
    this.fieldFactory = new FieldFactory();
    this.getterFactory = new GetterFactory();
    this.setterFactory = new SetterFactory();
    this.constructorFactory = new ConstructorFactory();
    this.delegatingBuilderFactory = new DelegatingBuilderFactory();
    this.equalsHashCodeFactory = new EqualsHashCodeFactory();
    this.toStringFactory = new ToStringFactory();
  }

  /**
   * Create a Java source file for the given model.
   *
   * @param model the model to use.
   * @return the generated source file.
   */
  public JavaFile create(Model model) {
    this.logger.debug("Building Java file for {}", model);
    TypeSpec typeSpec = this.buildModelTypeSpecFrom(model);
    JavaFile javaFile = this.wrapTypeSpecInPackage(typeSpec, model);
    this.logger.trace("Generated source file {}\n{}", model.getQualifiedName(), javaFile);
    return javaFile;
  }

  private JavaFile wrapTypeSpecInPackage(TypeSpec typeSpec, Model model) {
    return JavaFile
        .builder(model.getPackageName(), typeSpec)
        .skipJavaLangImports(true)
        .indent(model.getIndent())
        .build();
  }

  private TypeSpec buildModelTypeSpecFrom(Model model) {
    TypeSpec.Builder builder = TypeSpec
        .classBuilder(model.getClassName())
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addSuperinterface(model.getSuperInterface().asType())
        .addAnnotation(CodeGenUtils.generated(model.getSuperInterface()));

    this.applyDeprecation(builder, model);
    this.applyAttributes(builder, model);
    this.applyConstructors(builder, model);
    this.applyBuilders(builder, model);
    this.applyEqualsHashCode(builder, model);
    this.applyToString(builder, model);

    return builder.build();
  }

  private void applyDeprecation(TypeSpec.Builder typeSpecBuilder, Model model) {
    model.getDeprecatedAnnotation()
        .map(CodeGenUtils::copyDeprecatedFrom)
        .ifPresent(typeSpecBuilder::addAnnotation);
  }

  private void applyAttributes(TypeSpec.Builder typeSpecBuilder, Model model) {
    for (Attribute attribute : model.getAttributes()) {
      typeSpecBuilder
          .addField(this.fieldFactory.create(attribute))
          .addMethod(this.getterFactory.create(attribute));

      if (!attribute.isFinalField()) {
        typeSpecBuilder
            .addMethod(this.setterFactory.create(attribute, model.getSetterPrefix()));
      }
    }
  }

  private void applyConstructors(TypeSpec.Builder typeSpecBuilder, Model model) {
    this.constructorFactory
        .create(model)
        .forEach(typeSpecBuilder::addMethod);
  }

  private void applyBuilders(TypeSpec.Builder typeSpecBuilder, Model model) {
    model.getBuilderStrategy()
        .map(strategy -> this.delegatingBuilderFactory.create(model, strategy))
        .ifPresent(members -> members.applyTo(typeSpecBuilder));
  }

  private void applyEqualsHashCode(TypeSpec.Builder typeSpecBuilder, Model model) {
    this.equalsHashCodeFactory
        .create(model)
        .ifPresent(members -> members.applyTo(typeSpecBuilder));
  }

  private void applyToString(TypeSpec.Builder typeSpecBuilder, Model model) {
    this.toStringFactory
        .create(model)
        .ifPresent(typeSpecBuilder::addMethod);
  }
}
