package io.ascopes.katana.ap.codegen.components;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import io.ascopes.katana.ap.codegen.init.InitTracker;
import io.ascopes.katana.ap.codegen.init.InitTrackerFactory;
import io.ascopes.katana.ap.descriptors.Attribute;
import io.ascopes.katana.ap.descriptors.BuilderStrategy;
import io.ascopes.katana.ap.descriptors.Model;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import java.util.LinkedHashSet;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;

/**
 * Factory for creating a builder and the associated conduit.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class BuilderFactory {

  private static final String THIS = "this";
  private static final String TRACKING_FIELD = "$$initialized";
  private static final String UNINITIALIZED_SET_VARIABLE = "$$uninitialized";
  private static final String UNINITIALIZED_NAMES_VARIABLE = "$$uninitializedNames";

  private final InitTrackerFactory initTrackerFactory;
  private final Logger logger;

  /**
   * Initialize this factory.
   */
  public BuilderFactory() {
    this.initTrackerFactory = new InitTrackerFactory();
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  /**
   * Create a set of builder components used to define a builder and initialize the model from the
   * builder, using a given model.
   *
   * @param model    the model to generate the builder for.
   * @param strategy the strategy to use to create the builder.
   * @return the generated builder components.
   */
  public BuilderComponents create(Model model, BuilderStrategy strategy) {

    Stream<Attribute> requiredAttrs = model
        .getAttributes()
        .stream();
    // TODO: specify whether fields are required in a builder or not.
    //.filter(Attribute::isFinalField);

    InitTracker initTracker = this.initTrackerFactory.createTracker(requiredAttrs, TRACKING_FIELD);

    BuilderComponents.Builder builder = BuilderComponents
        .builder()
        .builderType(this.createBuilderType(model, strategy, initTracker))
        .builderInitializer(this.createModelBuilderStaticMethod(model, strategy))
        .builderConstructor(this.createModelBuilderConstructor(model, strategy, initTracker));

    if (strategy.isToBuilderMethodEnabled()) {
      builder.toBuilderMethod(this.createToBuilderMethod(model, strategy));
    }

    return builder.build();
  }

  private TypeSpec createBuilderType(
      Model model,
      BuilderStrategy strategy,
      InitTracker initTracker
  ) {
    TypeSpec.Builder builder = TypeSpec
        .classBuilder(this.builderTypeName(model, strategy))
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .addMethod(this.createPrivateBuilderConstructor())
        .addMethod(this.createBuilderBuildMethod(this.modelTypeName(model), strategy));

    this.createFields(model)
        .forEach(builder::addField);

    this.createSetters(model, strategy, initTracker)
        .forEach(builder::addMethod);

    if (!initTracker.isEmpty()) {
      builder.addField(this.createTrackingVariable(initTracker));
    }

    return builder.build();
  }

  private FieldSpec createTrackingVariable(InitTracker initTracker) {
    return FieldSpec
        .builder(initTracker.getTypeName(), initTracker.getFieldName())
        .addModifiers(Modifier.PRIVATE, Modifier.TRANSIENT)
        .initializer(initTracker.getTrackingVariableInitialValue())
        .build();
  }

  private MethodSpec createPrivateBuilderConstructor() {
    return MethodSpec
        .constructorBuilder()
        .addModifiers(Modifier.PRIVATE)
        .build();
  }

  private MethodSpec createBuilderBuildMethod(ClassName modelTypeName, BuilderStrategy strategy) {
    return MethodSpec
        .methodBuilder(strategy.getBuildMethodName())
        .returns(modelTypeName)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addStatement("return new $T($L)", modelTypeName, THIS)
        .build();
  }

  private Stream<FieldSpec> createFields(Model model) {
    return model
        .getAttributes()
        .stream()
        .map(this::createField);
  }

  private FieldSpec createField(Attribute attribute) {
    return FieldSpec
        .builder(attribute.getType(), attribute.getIdentifier())
        .addModifiers(Modifier.PRIVATE)
        .build();
  }

  private Stream<MethodSpec> createSetters(
      Model model,
      BuilderStrategy strategy,
      InitTracker initTracker
  ) {
    return model
        .getAttributes()
        .stream()
        .map(attr -> this.createSetter(model, attr, strategy, initTracker));
  }

  private MethodSpec createSetter(
      Model model,
      Attribute attribute,
      BuilderStrategy strategy,
      InitTracker initTracker
  ) {
    ParameterSpec parameter = ParameterSpec
        .builder(attribute.getType(), attribute.getIdentifier(), Modifier.FINAL)
        .build();

    MethodSpec.Builder builder = MethodSpec
        .methodBuilder(attribute.getIdentifier())
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .returns(this.builderTypeName(model, strategy))
        .addParameter(parameter);

    // TODO(ascopes): optional checks?

    initTracker
        .getInitializedExpr(THIS, attribute)
        .map(check -> CodeBlock
            .builder()
            .beginControlFlow("if ($L)", check)
            .addStatement(
                "throw new $T($S)",
                IllegalStateException.class,
                "Attribute " + attribute.getName() + " was already initialized"
            )
            .endControlFlow()
            .build())
        .ifPresent(builder::addCode);

    // TODO(ascopes): nullness checks?

    builder.addStatement("$1L.$2L = $2L", THIS, attribute.getIdentifier());

    initTracker
        .getUpdateInitializedExpr(THIS, attribute)
        .ifPresent(builder::addStatement);

    builder.addStatement("return $L", THIS);

    return builder.build();
  }

  private MethodSpec createModelBuilderStaticMethod(Model model, BuilderStrategy strategy) {
    ClassName builderTypeName = this.builderTypeName(model, strategy);

    return MethodSpec
        // TODO(ascopes): allow customizing this name.
        // TODO(ascopes): handle collisions with this name.
        .methodBuilder(strategy.getBuilderMethodName())
        .returns(builderTypeName)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .addStatement("return new $T()", builderTypeName)
        .build();
  }

  private MethodSpec createModelBuilderConstructor(
      Model model,
      BuilderStrategy strategy,
      InitTracker initTracker
  ) {
    ParameterSpec parameter = ParameterSpec
        .builder(this.builderTypeName(model, strategy), "builder", Modifier.FINAL)
        .build();

    MethodSpec.Builder methodBuilder = MethodSpec
        .constructorBuilder()
        .addModifiers(Modifier.PRIVATE)
        .addParameter(parameter);

    CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();

    if (!initTracker.isEmpty()) {
      CodeBlock anyUninitialized = initTracker.getAnyUninitializedExpr(parameter.name);

      codeBlockBuilder
          .beginControlFlow("if ($L)", anyUninitialized)
          .addStatement(
              "$1T<$2T> $3L = new $1T<>()",
              LinkedHashSet.class,
              String.class,
              UNINITIALIZED_SET_VARIABLE);

      for (Attribute attribute : model.getAttributes()) {
        initTracker
            .getUninitializedExpr(parameter.name, attribute)
            .ifPresent(expr -> codeBlockBuilder
                .beginControlFlow("if ($L)", expr)
                .addStatement("$L.add($S)", UNINITIALIZED_SET_VARIABLE, attribute.getName())
                .endControlFlow());
      }

      codeBlockBuilder
          .beginControlFlow("if (!$L.isEmpty())", UNINITIALIZED_SET_VARIABLE)
          .addStatement(
              "$1T $2L = $1T.join($3S, $4N)",
              String.class,
              UNINITIALIZED_NAMES_VARIABLE,
              ", ",
              UNINITIALIZED_SET_VARIABLE
          )
          .addStatement(
              "throw new $T($S)",
              IllegalArgumentException.class,
              UNINITIALIZED_NAMES_VARIABLE
          )
          .endControlFlow()
          .endControlFlow();
    }

    for (Attribute attribute : model.getAttributes()) {
      codeBlockBuilder.addStatement(
          "$1L.$2N = $3L.$2N",
          THIS,
          attribute.getIdentifier(),
          parameter.name
      );
    }

    return methodBuilder
        .addCode(codeBlockBuilder.build())
        .build();
  }

  private MethodSpec createToBuilderMethod(Model model, BuilderStrategy strategy) {
    MethodSpec.Builder methodBuilder = MethodSpec
        .methodBuilder(strategy.getToBuilderMethodName())
        .returns(this.builderTypeName(model, strategy))
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

    CodeBlock.Builder codeBlockBuilder = CodeBlock
        .builder()
        .add("return $T.$N()\n", this.modelTypeName(model), strategy.getBuilderMethodName());

    for (Attribute attribute : model.getAttributes()) {
      codeBlockBuilder.add(".$1N($2L.$1N)\n", attribute.getIdentifier(), THIS);
    }

    codeBlockBuilder.addStatement(".$N()", strategy.getBuildMethodName());

    return methodBuilder
        .addCode(codeBlockBuilder.build())
        .build();
  }

  private ClassName modelTypeName(Model model) {
    return ClassName.get(model.getPackageName(), model.getClassName());
  }

  private ClassName builderTypeName(Model model, BuilderStrategy strategy) {
    return this.modelTypeName(model).nestedClass(strategy.getBuilderTypeName());
  }
}
