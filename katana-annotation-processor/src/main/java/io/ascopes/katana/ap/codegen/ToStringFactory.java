package io.ascopes.katana.ap.codegen;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import io.ascopes.katana.ap.analysis.Attribute;
import io.ascopes.katana.ap.analysis.Model;
import io.ascopes.katana.ap.analysis.ToStringStrategy;
import io.ascopes.katana.ap.analysis.ToStringStrategy.CustomToStringStrategy;
import io.ascopes.katana.ap.analysis.ToStringStrategy.GeneratedToStringStrategy;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.utils.CodeGenUtils;
import java.util.Optional;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

/**
 * Factory for generating {@link Object#toString()} overloads.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
final class ToStringFactory {

  private final Logger logger;

  /**
   * Initialize this factory.
   */
  ToStringFactory() {
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  /**
   * Create a toString method for the given model, if enabled.
   *
   * @param model the model to create for.
   * @return an optional containing the toString method, if enabled, or an empty optional if
   *     disabled.
   */
  Optional<MethodSpec> create(Model model) {
    return model
        .getToStringStrategy()
        .map(strategy -> this.createForStrategy(model, strategy));
  }

  private MethodSpec createForStrategy(Model model, ToStringStrategy toStringStrategy) {
    if (toStringStrategy instanceof GeneratedToStringStrategy) {
      return this.createGenerated(model);
    }

    if (toStringStrategy instanceof CustomToStringStrategy) {
      return this.createCustom((CustomToStringStrategy) toStringStrategy);
    }

    throw new UnsupportedOperationException("Unable to handle strategy " + toStringStrategy);
  }

  private MethodSpec createGenerated(Model model) {
    this.logger.trace("Generating a generated toString method");

    CodeBlock attrsPart = model
        .getAttributes()
        .stream()
        .filter(Attribute::isIncludeInToString)
        .map(this::toStringAttribute)
        .collect(CodeBlock.joining(" + \", \"\n", "\n", "\n"));

    CodeBlock toStringStatement = CodeBlock
        .builder()
        .add("return $S", model.getClassName() + "{")
        .indent()
        .add(attrsPart)
        .addStatement("+ $S", "}")
        .unindent()
        .build();

    MethodSpec generatedMethod = this.toStringBuilder()
        .addCode(toStringStatement)
        .build();

    this.logger.trace("Generated toString method:\n{}", generatedMethod);

    return generatedMethod;
  }

  private MethodSpec createCustom(CustomToStringStrategy toStringStrategy) {
    this.logger.trace("Generating a delegating toString method");

    ExecutableElement toCall = toStringStrategy.getToStringMethod();
    TypeMirror toCallType = toCall.getEnclosingElement().asType();
    CharSequence toCallName = toCall.getSimpleName();

    MethodSpec generatedMethod = this
        .toStringBuilder()
        .addStatement("return $T.$N(this)", toCallType, toCallName)
        .build();

    this.logger.trace("Generated toString method:\n{}", generatedMethod);

    return generatedMethod;
  }

  private CodeBlock toStringAttribute(Attribute attribute) {
    return CodeBlock.of("+ $S + this.$N", attribute.getName() + "=", attribute.getIdentifier());
  }

  private MethodSpec.Builder toStringBuilder() {
    return MethodSpec
        .methodBuilder("toString")
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addAnnotation(CodeGenUtils.override())
        .returns(String.class);
  }
}
