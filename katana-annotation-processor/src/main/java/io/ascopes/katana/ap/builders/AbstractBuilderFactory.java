package io.ascopes.katana.ap.builders;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.ascopes.katana.ap.attributes.AttributeDescriptor;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.types.ModelDescriptor;
import io.ascopes.katana.ap.types.TypeSpecMembers;
import io.ascopes.katana.ap.types.TypeSpecMembers.TypeSpecMembersBuilder;
import io.ascopes.katana.ap.utils.CodeGenUtils;
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

  /**
   * {@inheritDoc}
   */
  @Override
  public final TypeSpecMembers create(
      ModelDescriptor model,
      BuilderStrategyDescriptor strategy,
      T context
  ) {
    return this.createMembersFor(model, strategy, context).build();
  }

  /**
   * Create members for a given model descriptor.
   *
   * @param model    the model descriptor.
   * @param strategy the strategy.
   * @param context  the context.
   * @return the type spec members builder.
   */
  public TypeSpecMembersBuilder createMembersFor(
      ModelDescriptor model,
      BuilderStrategyDescriptor strategy,
      T context
  ) {
    TypeSpec builderType = this.builderTypeFor(model, strategy, context).build();
    this.logger.trace("Generated builder type:\n{}", builderType);

    MethodSpec builderMethod = this.builderMethodFor(model, strategy, context).build();
    this.logger.trace("Generated builder method:\n{}", builderMethod);

    MethodSpec modelConstructor = this.modelConstructorFor(model, strategy).build();
    this.logger.trace("Generated constructor:\n{}", modelConstructor);

    TypeSpecMembersBuilder membersTypeSpecMembersBuilder = TypeSpecMembers
        .builder()
        .type(builderType)
        .method(builderMethod)
        .method(modelConstructor);

    if (strategy.isToBuilderMethodEnabled()) {
      MethodSpec toBuilderMethod = this.toBuilderMethodFor(model, strategy).build();

      this.logger.trace("Generated toBuilder method:\n{}", toBuilderMethod);

      membersTypeSpecMembersBuilder
          .method(toBuilderMethod);
    }

    return membersTypeSpecMembersBuilder;
  }

  MethodSpec.Builder modelConstructorFor(
      ModelDescriptor model,
      BuilderStrategyDescriptor strategy
  ) {
    String builderParamName = "builder";
    TypeName builderTypeName = this.builderTypeNameFor(model, strategy);

    return MethodSpec
        .constructorBuilder()
        .addModifiers(Modifier.PRIVATE)
        .addParameter(builderTypeName, builderParamName, Modifier.FINAL)
        .addCode(this.modelConstructorBodyFor(model, builderParamName).build());
  }

  CodeBlock.Builder modelConstructorBodyFor(
      ModelDescriptor model,
      String builderParamName
  ) {
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

  MethodSpec.Builder builderMethodFor(
      ModelDescriptor model,
      BuilderStrategyDescriptor strategy,
      T context
  ) {
    TypeName builderType = this.builderTypeNameFor(model, strategy);
    CodeBlock body = this.builderMethodBodyFor(builderType).build();

    return MethodSpec
        .methodBuilder(strategy.getBuilderMethodName())
        .returns(builderType)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .addCode(body);
  }

  CodeBlock.Builder builderMethodBodyFor(TypeName builderType) {
    return CodeBlock
        .builder()
        .addStatement("return new $T()", builderType);
  }

  MethodSpec.Builder toBuilderMethodFor(
      ModelDescriptor model,
      BuilderStrategyDescriptor strategy
  ) {
    TypeName builderType = this.builderTypeNameFor(model, strategy);

    CodeBlock body = model
        .getAttributes()
        .stream()
        .map(attr -> this.setAttributeOnBuilderFor(attr))
        .reduce(CodeBlock.builder().add("return new $T()", builderType), (a, b) -> a.add(b.build()))
        .build();

    return MethodSpec
        .methodBuilder(strategy.getToBuilderMethodName())
        .returns(builderType)
        .addStatement(body);
  }

  CodeBlock.Builder setAttributeOnBuilderFor(
      AttributeDescriptor attribute
  ) {
    String setterName = this.builderSetterNameFor(attribute);
    return CodeBlock.builder().add(".$N(this.$N)", setterName, attribute.getIdentifier());
  }

  TypeSpec.Builder builderTypeFor(
      ModelDescriptor model,
      BuilderStrategyDescriptor strategy,
      T context
  ) {
    TypeSpec.Builder typeSpecBuilder = TypeSpec
        .classBuilder(strategy.getBuilderClassName())
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .addMethod(this.builderConstructor(context).build())
        .addMethod(this.builderBuildFor(model, strategy, context).build());

    model
        .getDeprecatedAnnotation()
        .map(CodeGenUtils::copyDeprecatedFrom)
        .ifPresent(typeSpecBuilder::addAnnotation);

    model.getAttributes()
        .forEach(attr -> this.applyFieldAndMethod(model, attr, strategy, typeSpecBuilder, context));

    return typeSpecBuilder;
  }

  void applyFieldAndMethod(
      ModelDescriptor model,
      AttributeDescriptor attribute,
      BuilderStrategyDescriptor strategy,
      TypeSpec.Builder builderType,
      T context
  ) {
    builderType
        .addField(this.builderFieldFor(attribute).build())
        .addMethod(this.builderSetterFor(model, attribute, strategy, context).build());
  }

  FieldSpec.Builder builderFieldFor(AttributeDescriptor attributeDescriptor) {
    return FieldSpec
        .builder(attributeDescriptor.getType(), attributeDescriptor.getIdentifier())
        .addModifiers(Modifier.PRIVATE);
  }

  MethodSpec.Builder builderSetterFor(
      ModelDescriptor model,
      AttributeDescriptor attribute,
      BuilderStrategyDescriptor strategy,
      T context
  ) {
    TypeName builderTypeName = this.builderTypeNameFor(model, strategy);
    String setterName = this.builderSetterNameFor(attribute);
    String paramName = this.builderParamNameFor(attribute);
    String fieldName = this.builderFieldNameFor(attribute);
    ParameterSpec parameter = this.builderSetterParamFor(attribute).build();
    CodeBlock body = this.builderSetterBodyFor(attribute, paramName, fieldName, context).build();

    MethodSpec.Builder method = MethodSpec
        .methodBuilder(setterName)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .returns(builderTypeName)
        .addParameter(parameter)
        .addCode(body);

    attribute
        .getDeprecatedAnnotation()
        .map(CodeGenUtils::copyDeprecatedFrom)
        .ifPresent(method::addAnnotation);

    return method;
  }

  CodeBlock.Builder builderSetterBodyFor(
      AttributeDescriptor attribute,
      String paramName,
      String fieldName,
      T context
  ) {
    return CodeBlock
        .builder()
        .addStatement("this.$N = $N", fieldName, paramName)
        .addStatement("return this");
  }

  MethodSpec.Builder builderBuildFor(
      ModelDescriptor model,
      BuilderStrategyDescriptor strategy,
      T context
  ) {
    MethodSpec.Builder method = MethodSpec
        .methodBuilder(strategy.getBuildMethodName())
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .returns(model.getQualifiedName())
        .addCode(this.builderBuildBodyFor(model, context).build());

    model
        .getDeprecatedAnnotation()
        .map(CodeGenUtils::copyDeprecatedFrom)
        .ifPresent(method::addAnnotation);

    return method;
  }

  CodeBlock.Builder builderBuildBodyFor(ModelDescriptor model, T context) {
    return CodeBlock
        .builder()
        .addStatement("return new $T(this)", model.getQualifiedName());
  }

  MethodSpec.Builder builderConstructor(T context) {
    return MethodSpec
        .constructorBuilder()
        .addModifiers(Modifier.PRIVATE);
  }

  ParameterSpec.Builder builderSetterParamFor(AttributeDescriptor attribute) {
    String paramName = this.builderParamNameFor(attribute);
    return ParameterSpec
        .builder(attribute.getType(), paramName, Modifier.FINAL);
  }

  ClassName builderTypeNameFor(ModelDescriptor model, BuilderStrategyDescriptor strategy) {
    return model.getQualifiedName().nestedClass(strategy.getBuilderClassName());
  }

  String builderFieldNameFor(AttributeDescriptor attribute) {
    return attribute.getIdentifier();
  }

  String builderParamNameFor(AttributeDescriptor attribute) {
    return attribute.getIdentifier();
  }

  String builderSetterNameFor(AttributeDescriptor attribute) {
    // TODO(ascopes): adjust names to avoid collisions.
    return attribute.getIdentifier();
  }
}
