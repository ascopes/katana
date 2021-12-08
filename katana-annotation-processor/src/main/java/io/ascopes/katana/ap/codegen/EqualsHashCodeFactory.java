package io.ascopes.katana.ap.codegen;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import io.ascopes.katana.ap.analysis.Attribute;
import io.ascopes.katana.ap.analysis.EqualityStrategy;
import io.ascopes.katana.ap.analysis.EqualityStrategy.CustomEqualityStrategy;
import io.ascopes.katana.ap.analysis.EqualityStrategy.GeneratedEqualityStrategy;
import io.ascopes.katana.ap.analysis.Model;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.utils.CodeGenUtils;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

/**
 * Factory for generating {@link Object#equals(Object)} and {@link Object#hashCode()} overrides.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
final class EqualsHashCodeFactory {

  private static final String EQUALS_PARAM = "other";
  private static final String CAST_EQUALS_PARAM = "that";

  private final Logger logger;

  /**
   * Initialize this strategy.
   */
  EqualsHashCodeFactory() {
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  /**
   * Create the overrides and return them.
   *
   * @param model the model to create the overrides for.
   * @return the members to add, or an empty optional if equality and hashcode generation is
   *     disabled.
   */
  Optional<TypeSpecMembers> create(Model model) {
    return model
        .getEqualityStrategy()
        .map(strategy -> this.createForStrategy(model, strategy));
  }

  TypeSpecMembers createForStrategy(Model model, EqualityStrategy equalityStrategy) {
    if (equalityStrategy instanceof GeneratedEqualityStrategy) {
      return TypeSpecMembers
          .builder()
          .method(this.createGeneratedEquals(model))
          .method(this.createGeneratedHashCode(model))
          .build();
    }

    if (equalityStrategy instanceof CustomEqualityStrategy) {
      CustomEqualityStrategy castStrategy = (CustomEqualityStrategy) equalityStrategy;

      return TypeSpecMembers
          .builder()
          .method(this.createCustomEquals(castStrategy))
          .method(this.createCustomHashCode(castStrategy))
          .build();
    }

    throw new UnsupportedOperationException("Unable to handle strategy " + equalityStrategy);
  }

  MethodSpec createGeneratedEquals(Model model) {
    this.logger.trace("Generating a generated equals method");

    CodeBlock equalityExpression = this
        .getAttributesToInclude(model)
        .map(attr -> CodeBlock.of(
            "$1T.equals(this.$2N(), $3N.$2N())",
            Objects.class,
            attr.getGetterToOverride().getSimpleName(),
            CAST_EQUALS_PARAM
        ))
        .collect(CodeBlock.joining("\n&& "));

    CodeBlock codeBlock = CodeBlock
        .builder()
        .beginControlFlow(
            "if ($N instanceof $T)",
            EQUALS_PARAM,
            model.getSuperInterface()
        )
        .addStatement(
            "$1T $2N = ($1T) $3N",
            model.getSuperInterface(),
            CAST_EQUALS_PARAM,
            EQUALS_PARAM
        )
        .add("return ")
        .addStatement(equalityExpression)
        .endControlFlow()
        .addStatement("return false")
        .build();

    MethodSpec generatedMethod = this
        .equalsBuilder()
        .addCode(codeBlock)
        .build();

    this.logger.trace("Generated equals method:\n{}", generatedMethod);

    return generatedMethod;
  }

  MethodSpec createGeneratedHashCode(Model model) {
    this.logger.trace("Generating a generated hashCode method");

    CodeBlock hashCodeExpression = this
        .getAttributesToInclude(model)
        .map(attr -> CodeBlock.of("this.$N", attr.getIdentifier()))
        .collect(CodeBlock.joining(",\n", "\n", ""));

    MethodSpec generatedMethod = this
        .hashCodeBuilder()
        .addStatement("return $T.hash($L)", Objects.class, hashCodeExpression)
        .build();

    this.logger.trace("Generated hashCode method:\n{}", generatedMethod);

    return generatedMethod;
  }

  MethodSpec createCustomEquals(CustomEqualityStrategy equalityStrategy) {
    this.logger.trace("Generating a delegating equals method");

    ExecutableElement toCall = equalityStrategy.getEqualsMethod();
    TypeMirror toCallType = toCall.getEnclosingElement().asType();
    CharSequence toCallName = toCall.getSimpleName();

    MethodSpec generatedMethod = this
        .equalsBuilder()
        .addStatement("return $T.$N(this, $N)", toCallType, toCallName, EQUALS_PARAM)
        .build();

    this.logger.trace("Generated equals method:\n{}", generatedMethod);

    return generatedMethod;
  }

  MethodSpec createCustomHashCode(CustomEqualityStrategy equalityStrategy) {
    this.logger.trace("Generating a delegating hashCode method");

    ExecutableElement toCall = equalityStrategy.getHashCodeMethod();
    TypeMirror toCallType = toCall.getEnclosingElement().asType();
    CharSequence toCallName = toCall.getSimpleName();

    MethodSpec generatedMethod = this
        .hashCodeBuilder()
        .addStatement("return $T.$N(this)", toCallType, toCallName)
        .build();

    this.logger.trace("Generated hashCode method:\n{}", generatedMethod);

    return generatedMethod;
  }

  private Stream<Attribute> getAttributesToInclude(Model model) {
    return model
        .getAttributes()
        .stream()
        .filter(Attribute::isIncludeInEqualsAndHashCode);
  }

  private MethodSpec.Builder equalsBuilder() {
    return MethodSpec
        .methodBuilder("equals")
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addAnnotation(CodeGenUtils.override())
        .addParameter(Object.class, EQUALS_PARAM, Modifier.FINAL)
        .returns(boolean.class);
  }

  private MethodSpec.Builder hashCodeBuilder() {
    return MethodSpec
        .methodBuilder("hashCode")
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addAnnotation(CodeGenUtils.override())
        .returns(int.class);
  }
}
