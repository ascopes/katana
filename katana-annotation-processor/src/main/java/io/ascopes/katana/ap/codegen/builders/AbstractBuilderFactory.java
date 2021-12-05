package io.ascopes.katana.ap.codegen.builders;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.ascopes.katana.ap.codegen.TypeSpecMembers;
import io.ascopes.katana.ap.descriptors.Attribute;
import io.ascopes.katana.ap.descriptors.BuilderStrategy;
import io.ascopes.katana.ap.descriptors.Model;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import javax.lang.model.element.Modifier;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Base logic that most builders will want to implement or override.
 *
 * @param <T> contextual argument to pass around.
 * @author Ashley Scopes
 * @since 0.0.1
 */
abstract class AbstractBuilderFactory<@Nullable T> implements BuilderFactory<T> {

  private final Logger logger;

  AbstractBuilderFactory() {
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  @Override
  public final TypeSpecMembers create(Model model, BuilderStrategy strategy, T context) {
    return this.createMembersFor(model, strategy, context).build();
  }

  protected TypeSpecMembers.Builder createMembersFor(Model model, BuilderStrategy strategy, T context) {
    TypeSpec builderType = this.builderTypeFor(model, strategy, context).build();
    this.logger.trace("Generated builder type:\n{}", builderType);

    MethodSpec builderMethod = this.builderMethodFor(model, strategy, context).build();
    this.logger.trace("Generated builder method:\n{}", builderMethod);

    MethodSpec modelConstructor = this.modelConstructorFor(model, strategy).build();
    this.logger.trace("Generated constructor:\n{}", modelConstructor);

    TypeSpecMembers.Builder membersBuilder = TypeSpecMembers
        .builder()
        .type(builderType)
        .method(builderMethod)
        .method(modelConstructor);

    if (strategy.isToBuilderMethodEnabled()) {
      MethodSpec toBuilderMethod = this.toBuilderMethodFor(model, strategy, context).build();

      this.logger.trace("Generated toBuilder method:\n{}", toBuilderMethod);

      membersBuilder
          .method(toBuilderMethod);
    }

    return membersBuilder;
  }

  MethodSpec.Builder modelConstructorFor(Model model, BuilderStrategy strategy) {
    String builderParamName = "builder";
    TypeName builderTypeName = this.builderTypeNameFor(model, strategy);

    return MethodSpec
        .constructorBuilder()
        .addModifiers(Modifier.PRIVATE)
        .addParameter(builderTypeName, builderParamName, Modifier.FINAL)
        .addCode(this.modelConstructorBodyFor(model, builderParamName).build());
  }

  CodeBlock.Builder modelConstructorBodyFor(Model model, String builderParamName) {
    CodeBlock.Builder bodyBuilder = CodeBlock.builder();
    model
        .getAttributes()
        .forEach(attr -> bodyBuilder
            .addStatement(
                "this.$N = $N.$N",
                attr.getIdentifier(),
                builderParamName,
                this.builderFieldNameFor(attr)));

    return bodyBuilder;
  }

  MethodSpec.Builder builderMethodFor(Model model, BuilderStrategy strategy, T context) {
    TypeName builderType = this.builderTypeNameFor(model, strategy);
    CodeBlock body = this.builderMethodBodyFor(builderType, context).build();

    return MethodSpec
        .methodBuilder(strategy.getBuilderMethodName())
        .returns(builderType)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .addCode(body);
  }

  CodeBlock.Builder builderMethodBodyFor(TypeName builderType, T context) {
    return CodeBlock
        .builder()
        .addStatement("return new $T()", builderType);
  }

  MethodSpec.Builder toBuilderMethodFor(Model model, BuilderStrategy strategy, T context) {
    TypeName builderType = this.builderTypeNameFor(model, strategy);

    CodeBlock body = model
        .getAttributes()
        .stream()
        .map(attr -> this.setAttributeOnBuilderFor(attr, strategy))
        .reduce(CodeBlock.builder().add("return new $T()", builderType), (a, b) -> a.add(b.build()))
        .build();

    return MethodSpec
        .methodBuilder(strategy.getToBuilderMethodName())
        .returns(builderType)
        .addStatement(body);
  }

  CodeBlock.Builder setAttributeOnBuilderFor(
      Attribute attribute,
      BuilderStrategy strategy
  ) {
    String setterName = this.builderSetterNameFor(attribute, strategy);
    return CodeBlock.builder().add(".$N(this.$N)", setterName, attribute.getIdentifier());
  }

  TypeSpec.Builder builderTypeFor(Model model, BuilderStrategy strategy, T context) {
    TypeSpec.Builder typeSpecBuilder = TypeSpec
        .classBuilder(strategy.getBuilderClassName())
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .addMethod(this.builderConstructor(context).build())
        .addMethod(this.builderBuildFor(model, strategy, context).build());

    model.getAttributes()
        .forEach(attr -> this.applyAttributeTo(model, strategy, attr, typeSpecBuilder, context));

    return typeSpecBuilder;
  }

  void applyAttributeTo(
      Model model,
      BuilderStrategy strategy,
      Attribute attribute,
      TypeSpec.Builder builderType,
      T context
  ) {
    builderType
        .addField(this.builderFieldFor(attribute, context).build())
        .addMethod(this.builderSetterFor(model, attribute, strategy, context).build());
  }

  FieldSpec.Builder builderFieldFor(Attribute attribute, T context) {
    return FieldSpec
        .builder(attribute.getType(), attribute.getIdentifier())
        .addModifiers(Modifier.PRIVATE);
  }

  MethodSpec.Builder builderSetterFor(
      Model model,
      Attribute attribute,
      BuilderStrategy strategy,
      T context
  ) {
    TypeName builderTypeName = this.builderTypeNameFor(model, strategy);
    String setterName = this.builderSetterNameFor(attribute, strategy);
    String paramName = this.builderParamNameFor(attribute);
    String fieldName = this.builderFieldNameFor(attribute);
    ParameterSpec parameter = this.builderSetterParamFor(attribute).build();
    CodeBlock body = this.builderSetterBodyFor(attribute, paramName, fieldName, context).build();

    return MethodSpec
        .methodBuilder(setterName)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .returns(builderTypeName)
        .addParameter(parameter)
        .addCode(body);
  }

  CodeBlock.Builder builderSetterBodyFor(
      Attribute attribute,
      String paramName,
      String fieldName,
      T context
  ) {
    return CodeBlock
        .builder()
        .addStatement("this.$N = $N", fieldName, paramName)
        .addStatement("return this");
  }

  MethodSpec.Builder builderBuildFor(Model model, BuilderStrategy strategy, T context) {
    return MethodSpec
        .methodBuilder(strategy.getBuildMethodName())
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .returns(model.getQualifiedName())
        .addCode(this.builderBuildBodyFor(model, context).build());
  }

  CodeBlock.Builder builderBuildBodyFor(Model model, T context) {
    return CodeBlock
        .builder()
        .addStatement("return new $T(this)", model.getQualifiedName());
  }

  MethodSpec.Builder builderConstructor(T context) {
    return MethodSpec
        .constructorBuilder()
        .addModifiers(Modifier.PRIVATE);
  }

  ParameterSpec.Builder builderSetterParamFor(Attribute attribute) {
    String paramName = this.builderParamNameFor(attribute);
    return ParameterSpec
        .builder(attribute.getType(), paramName, Modifier.FINAL);
  }

  ClassName builderTypeNameFor(Model model, BuilderStrategy strategy) {
    return model.getQualifiedName().nestedClass(strategy.getBuilderClassName());
  }

  String builderFieldNameFor(Attribute attribute) {
    return attribute.getIdentifier();
  }

  String builderParamNameFor(Attribute attribute) {
    return attribute.getIdentifier();
  }

  String builderSetterNameFor(Attribute attribute, BuilderStrategy strategy) {
    // TODO(ascopes): adjust names to avoid collisions.
    return attribute.getIdentifier();
  }
}
