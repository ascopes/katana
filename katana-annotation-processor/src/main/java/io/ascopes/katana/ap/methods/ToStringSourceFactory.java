/*
 * Copyright (C) 2021 Ashley Scopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ascopes.katana.ap.methods;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import io.ascopes.katana.ap.attributes.AttributeDescriptor;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.methods.ToStringStrategyDescriptor.CustomToStringStrategyDescriptor;
import io.ascopes.katana.ap.methods.ToStringStrategyDescriptor.GeneratedToStringStrategyDescriptor;
import io.ascopes.katana.ap.types.ModelDescriptor;
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
public final class ToStringSourceFactory {

  private final Logger logger;

  /**
   * Initialize this factory.
   */
  public ToStringSourceFactory() {
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  /**
   * Create a toString method for the given model, if enabled.
   *
   * @param modelDescriptor the model to create for.
   * @return an optional containing the toString method, if enabled, or an empty optional if
   *     disabled.
   */
  public Optional<MethodSpec> create(ModelDescriptor modelDescriptor) {
    return modelDescriptor
        .getToStringStrategy()
        .map(strategy -> this.createForStrategy(modelDescriptor, strategy));
  }

  private MethodSpec createForStrategy(
      ModelDescriptor modelDescriptor,
      ToStringStrategyDescriptor toStringStrategy
  ) {
    if (toStringStrategy instanceof GeneratedToStringStrategyDescriptor) {
      return this.createGenerated(modelDescriptor);
    }

    if (toStringStrategy instanceof CustomToStringStrategyDescriptor) {
      return this.createCustom((CustomToStringStrategyDescriptor) toStringStrategy);
    }

    throw new UnsupportedOperationException("Unable to handle strategy " + toStringStrategy);
  }

  private MethodSpec createGenerated(ModelDescriptor modelDescriptor) {
    this.logger.trace("Generating a generated toString method");

    CodeBlock attrsPart = modelDescriptor
        .getAttributes()
        .stream()
        .filter(AttributeDescriptor::isIncludeInToString)
        .map(this::toStringAttribute)
        .collect(CodeBlock.joining(" + \", \"\n", "\n", "\n"));

    CodeBlock toStringStatement = CodeBlock
        .builder()
        .add("return $S", modelDescriptor.getClassName() + "{")
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

  private MethodSpec createCustom(CustomToStringStrategyDescriptor toStringStrategy) {
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

  private CodeBlock toStringAttribute(AttributeDescriptor attributeDescriptor) {
    return CodeBlock.of(
        "+ $S + this.$N",
        attributeDescriptor.getName() + "=",
        attributeDescriptor.getIdentifier()
    );
  }

  private MethodSpec.Builder toStringBuilder() {
    return MethodSpec
        .methodBuilder("toString")
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addAnnotation(CodeGenUtils.override())
        .returns(String.class);
  }
}
