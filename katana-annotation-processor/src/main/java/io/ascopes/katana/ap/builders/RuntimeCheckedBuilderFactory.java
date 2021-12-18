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

package io.ascopes.katana.ap.builders;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.ascopes.katana.ap.attributes.AttributeDescriptor;
import io.ascopes.katana.ap.types.ModelDescriptor;
import io.ascopes.katana.ap.utils.Functors;
import java.util.LinkedHashSet;
import javax.lang.model.element.Modifier;

/**
 * Simple builder factory implementation that provides initialization checking at runtime using
 * bitflags.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class RuntimeCheckedBuilderFactory extends AbstractBuilderFactory<InitTracker> {

  private static final String TRACKING_FIELD_NAME = "$$_Katana__initialized_fields";
  private static final String UNINITIALIZED_SET_NAME = "$$_Katana__uninitialized_fields";

  @Override
  TypeSpec.Builder builderTypeFor(
      ModelDescriptor model,
      BuilderStrategyDescriptor strategy,
      InitTracker tracker
  ) {
    return super
        .builderTypeFor(model, strategy, tracker)
        .addField(this.trackingField(tracker));
  }

  @Override
  MethodSpec.Builder builderConstructor(InitTracker tracker) {
    return super
        .builderConstructor(tracker)
        .addStatement(tracker.initializeTracker(this.trackingFieldReference()));
  }

  @Override
  Builder builderBuildFor(
      ModelDescriptor model,
      BuilderStrategyDescriptor strategy,
      InitTracker tracker
  ) {
    return super
        .builderBuildFor(model, strategy, tracker)
        .addException(this.initializationExceptionType());
  }

  @Override
  CodeBlock.Builder builderBuildBodyFor(ModelDescriptor model, InitTracker tracker) {
    return CodeBlock
        .builder()
        .beginControlFlow(
            "if $L", tracker.isAnyUninitialized(this.trackingFieldReference())
        )
        .add(this.determineWhichFieldsAreNotInitialized(model, tracker))
        .endControlFlow()
        .add(super.builderBuildBodyFor(model, tracker).build());
  }

  @Override
  CodeBlock.Builder builderSetterBodyFor(
      AttributeDescriptor attribute,
      String paramName,
      String fieldName,
      InitTracker tracker
  ) {
    return tracker
        .markAttributeInitialized(this.trackingFieldReference(), attribute)
        .map(markStatement -> CodeBlock.builder().addStatement(markStatement))
        .orElseGet(CodeBlock::builder)
        .add(super.builderSetterBodyFor(attribute, paramName, fieldName, tracker).build());
  }

  private CodeBlock determineWhichFieldsAreNotInitialized(
      ModelDescriptor model,
      InitTracker tracker
  ) {
    CodeBlock.Builder codeBlock = CodeBlock
        .builder()
        .addStatement(
            "$1T<$2T> $3N = new $1T<>()",
            LinkedHashSet.class,
            String.class,
            UNINITIALIZED_SET_NAME
        );

    model
        .getAttributes()
        .stream()
        .map(attr -> tracker
            .isAttributeUninitialized(this.trackingFieldReference(), attr)
            .map(check -> this.takeNoteIfFieldIsNotInitialized(check, attr)))
        .flatMap(Functors.removeEmpties())
        .forEach(codeBlock::add);

    return codeBlock
        .addStatement(
            "throw new $T($T.join($S, $N))",
            this.initializationExceptionType(),
            String.class,
            ", ",
            UNINITIALIZED_SET_NAME
        )
        .build();
  }

  private CodeBlock takeNoteIfFieldIsNotInitialized(
      CodeBlock initializationCheck,
      AttributeDescriptor attribute
  ) {
    return CodeBlock
        .builder()
        .beginControlFlow("if $L", initializationCheck)
        .addStatement("$N.add($S)", UNINITIALIZED_SET_NAME, attribute.getIdentifier())
        .endControlFlow()
        .build();
  }

  private FieldSpec trackingField(InitTracker tracker) {
    FieldSpec.Builder field = FieldSpec
        .builder(tracker.getTrackerType(), TRACKING_FIELD_NAME)
        .addModifiers(Modifier.PRIVATE, Modifier.TRANSIENT);

    if (tracker.isTrackingVariableFinal()) {
      field.addModifiers(Modifier.FINAL);
    }

    return field.build();
  }

  private CodeBlock trackingFieldReference() {
    return CodeBlock.of("this.$N", TRACKING_FIELD_NAME);
  }

  private TypeName initializationExceptionType() {
    // TODO(ascopes): Customize this exception?
    return TypeName.get(IllegalStateException.class);
  }
}
