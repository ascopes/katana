package io.ascopes.katana.ap.codegen.builders;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.ascopes.katana.ap.codegen.TypeSpecMembers;
import io.ascopes.katana.ap.descriptors.Attribute;
import io.ascopes.katana.ap.descriptors.BuilderStrategy;
import io.ascopes.katana.ap.descriptors.Model;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

/**
 * Base logic that most builders will want to implement or override.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
abstract class AbstractBuilderFactory implements BuilderFactory {

  private final Logger logger;

  AbstractBuilderFactory() {
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  @Override
  public final TypeSpecMembers create(Model model, BuilderStrategy strategy) {
    return this.createMembersFor(model, strategy).build();
  }

  TypeSpecMembers.Builder createMembersFor(Model model, BuilderStrategy strategy) {
    TypeSpec builderType = this.builderTypeFor(model, strategy).build();
    this.logger.trace("Generated builder type:\n{}", builderType);

    MethodSpec builderMethod = this.builderMethodFor(model, strategy).build();
    this.logger.trace("Generated builder method:\n{}", builderMethod);

    MethodSpec modelConstructor = this.modelConstructorFor(model, strategy).build();
    this.logger.trace("Generated constructor:\n{}", modelConstructor);

    TypeSpecMembers.Builder membersBuilder = TypeSpecMembers
        .builder()
        .type(builderType)
        .method(builderMethod)
        .method(modelConstructor);

    if (strategy.isToBuilderMethodEnabled()) {
      MethodSpec toBuilderMethod = this.toBuilderMethodFor(model, strategy).build();

      this.logger.trace("Generated toBuilder method:\n{}", toBuilderMethod);

      membersBuilder
          .method(toBuilderMethod);
    }

    return membersBuilder;
  }

  MethodSpec.Builder modelConstructorFor(Model model, BuilderStrategy strategy) {
    String builderParamName = "builder";

    return MethodSpec
        .constructorBuilder()
        .addModifiers(Modifier.PRIVATE)
        .addParameter(this.builderTypeNameFor(model, strategy), builderParamName, Modifier.FINAL)
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

  MethodSpec.Builder builderMethodFor(Model model, BuilderStrategy strategy) {
    TypeName builderType = this.builderTypeNameFor(model, strategy);

    return MethodSpec
        .methodBuilder(strategy.getBuilderMethodName())
        .returns(builderType)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .addCode(this.builderMethodBodyFor(builderType).build());
  }

  CodeBlock.Builder builderMethodBodyFor(TypeName builderType) {
    return CodeBlock
        .builder()
        .addStatement("return new $T()", builderType);
  }

  MethodSpec.Builder toBuilderMethodFor(Model model, BuilderStrategy strategy) {
    TypeName builderType = this.builderTypeNameFor(model, strategy);

    CodeBlock body = model
        .getAttributes()
        .stream()
        .map(this::setAttributeOnBuilderFor)
        .reduce(CodeBlock.builder().add("return new $T()", builderType), (a, b) -> a.add(b.build()))
        .build();

    return MethodSpec
        .methodBuilder(strategy.getToBuilderMethodName())
        .returns(builderType)
        .addStatement(body);
  }

  CodeBlock.Builder setAttributeOnBuilderFor(Attribute attribute) {
    String setterName = this.builderSetterNameFor(attribute);
    return CodeBlock.builder().add(".$N(this.$N)", setterName, attribute.getIdentifier());
  }

  TypeSpec.Builder builderTypeFor(Model model, BuilderStrategy strategy) {
    return TypeSpec
        .classBuilder(strategy.getBuilderClassName())
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .addFields(this.builderFieldsFor(model))
        .addMethod(this.builderConstructor().build())
        .addMethods(this.builderSettersFor(model, strategy))
        .addMethod(this.builderBuildFor(model, strategy).build());
  }

  Iterable<FieldSpec> builderFieldsFor(Model model) {
    return model
        .getAttributes()
        .stream()
        .map(attr -> FieldSpec
            .builder(attr.getType(), attr.getIdentifier())
            .addModifiers(Modifier.PRIVATE)
            .build())
        .collect(Collectors.toList());
  }

  Iterable<MethodSpec> builderSettersFor(Model model, BuilderStrategy strategy) {
    return model
        .getAttributes()
        .stream()
        .map(attr -> this.builderSetterFor(model, attr, strategy).build())
        .collect(Collectors.toList());
  }

  MethodSpec.Builder builderSetterFor(Model model, Attribute attribute, BuilderStrategy strategy) {
    TypeName builderTypeName = this.builderTypeNameFor(model, strategy);
    String setterName = this.builderSetterNameFor(attribute);
    String paramName = this.builderParamNameFor(attribute);
    String fieldName = this.builderFieldNameFor(attribute);

    return MethodSpec
        .methodBuilder(setterName)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .returns(builderTypeName)
        .addParameter(attribute.getType(), paramName, Modifier.FINAL)
        .addCode(this.builderSetterBodyFor(attribute, paramName, fieldName).build());
  }

  CodeBlock.Builder builderSetterBodyFor(
      Attribute attribute,
      String paramName,
      String fieldName
  ) {
    return CodeBlock
        .builder()
        .addStatement("this.$N = $N", fieldName, paramName)
        .addStatement("return this");
  }

  MethodSpec.Builder builderBuildFor(Model model, BuilderStrategy strategy) {
    return MethodSpec
        .methodBuilder(strategy.getBuildMethodName())
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .returns(model.getQualifiedName())
        .addCode(this.builderBuildBodyFor(model).build());
  }

  CodeBlock.Builder builderBuildBodyFor(Model model) {
    return CodeBlock
        .builder()
        .addStatement("return new $T(this)", model.getQualifiedName());
  }

  MethodSpec.Builder builderConstructor() {
    return MethodSpec
        .constructorBuilder()
        .addModifiers(Modifier.PRIVATE);
  }

  TypeName builderTypeNameFor(Model model, BuilderStrategy strategy) {
    return model.getQualifiedName().nestedClass(strategy.getBuilderClassName());
  }

  String builderFieldNameFor(Attribute attribute) {
    return attribute.getIdentifier();
  }

  String builderParamNameFor(Attribute attribute) {
    return attribute.getIdentifier();
  }

  String builderSetterNameFor(Attribute attribute) {
    // TODO(ascopes): adjust names to avoid collisions.
    return attribute.getIdentifier();
  }
}
