package io.ascopes.katana.ap.methods;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import io.ascopes.katana.ap.attributes.AttributeDescriptor;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.methods.EqualityStrategyDescriptor.CustomEqualityStrategyDescriptor;
import io.ascopes.katana.ap.methods.EqualityStrategyDescriptor.GeneratedEqualityStrategyDescriptor;
import io.ascopes.katana.ap.types.ModelDescriptor;
import io.ascopes.katana.ap.types.TypeSpecMembers;
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
public final class EqualsHashCodeSourceFactory {

  private static final String EQUALS_PARAM = "other";
  private static final String CAST_EQUALS_PARAM = "that";

  private final Logger logger;

  /**
   * Initialize this strategy.
   */
  public EqualsHashCodeSourceFactory() {
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  /**
   * Create the overrides and return them.
   *
   * @param modelDescriptor the model to create the overrides for.
   * @return the members to add, or an empty optional if equality and hashcode generation is
   *     disabled.
   */
  public Optional<TypeSpecMembers> create(ModelDescriptor modelDescriptor) {
    return modelDescriptor
        .getEqualityStrategy()
        .map(strategy -> this.createForStrategy(modelDescriptor, strategy));
  }

  TypeSpecMembers createForStrategy(
      ModelDescriptor modelDescriptor,
      EqualityStrategyDescriptor equalityStrategy
  ) {
    if (equalityStrategy instanceof GeneratedEqualityStrategyDescriptor) {
      return TypeSpecMembers
          .builder()
          .method(this.createGeneratedEquals(modelDescriptor))
          .method(this.createGeneratedHashCode(modelDescriptor))
          .build();
    }

    if (equalityStrategy instanceof CustomEqualityStrategyDescriptor) {
      return TypeSpecMembers
          .builder()
          .method(this.createCustomEquals((CustomEqualityStrategyDescriptor) equalityStrategy))
          .method(this.createCustomHashCode((CustomEqualityStrategyDescriptor) equalityStrategy))
          .build();
    }

    throw new UnsupportedOperationException("Unable to handle strategy " + equalityStrategy);
  }

  MethodSpec createGeneratedEquals(ModelDescriptor modelDescriptor) {
    this.logger.trace("Generating a generated equals method");

    CodeBlock equalityExpression = this
        .getAttributesToInclude(modelDescriptor)
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
            modelDescriptor.getSuperInterface()
        )
        .addStatement(
            "$1T $2N = ($1T) $3N",
            modelDescriptor.getSuperInterface(),
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

  MethodSpec createGeneratedHashCode(ModelDescriptor modelDescriptor) {
    this.logger.trace("Generating a generated hashCode method");

    CodeBlock hashCodeExpression = this
        .getAttributesToInclude(modelDescriptor)
        .map(attr -> CodeBlock.of("this.$N", attr.getIdentifier()))
        .collect(CodeBlock.joining(",\n", "\n", ""));

    MethodSpec generatedMethod = this
        .hashCodeBuilder()
        .addStatement("return $T.hash($L)", Objects.class, hashCodeExpression)
        .build();

    this.logger.trace("Generated hashCode method:\n{}", generatedMethod);

    return generatedMethod;
  }

  MethodSpec createCustomEquals(CustomEqualityStrategyDescriptor equalityStrategy) {
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

  MethodSpec createCustomHashCode(CustomEqualityStrategyDescriptor equalityStrategy) {
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

  private Stream<AttributeDescriptor> getAttributesToInclude(ModelDescriptor modelDescriptor) {
    return modelDescriptor
        .getAttributes()
        .stream()
        .filter(AttributeDescriptor::isIncludeInEqualsAndHashCode);
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
