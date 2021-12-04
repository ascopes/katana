package io.ascopes.katana.ap.codegen.builders;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.ascopes.katana.ap.codegen.TypeSpecMembers;
import io.ascopes.katana.ap.descriptors.Attribute;
import io.ascopes.katana.ap.descriptors.BuilderStrategy;
import io.ascopes.katana.ap.descriptors.Model;
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
final class RuntimeCheckedBuilderFactory implements BuilderFactory {

  private final InitTrackerFactory initTrackerFactory;

  RuntimeCheckedBuilderFactory(InitTrackerFactory initTrackerFactory) {
    this.initTrackerFactory = initTrackerFactory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TypeSpecMembers create(Model model, BuilderStrategy strategy) {
    InitTracker initTracker = this.initTrackerFactory.createTracker(model.getAttributes().stream());

    // Store the init tracker to create an object with side effects. Easier than reimplementing
    // every method to do the same stuff with an extra parameter.
    return new TrackedRuntimeCheckedBuilderFactory(initTracker)
        .create(model, strategy);
  }

  private static final class TrackedRuntimeCheckedBuilderFactory extends AbstractBuilderFactory {

    private static final String TRACKING_FIELD_NAME = "$$_Katana__initialized_fields";
    private static final String UNINITIALIZED_SET_NAME = "$$_Katana__uninitialized_fields";

    private final InitTracker initTracker;

    private TrackedRuntimeCheckedBuilderFactory(InitTracker initTracker) {
      this.initTracker = initTracker;
    }

    @Override
    TypeSpec.Builder builderTypeFor(Model model, BuilderStrategy strategy) {
      return super
          .builderTypeFor(model, strategy)
          .addField(this.trackingField());
    }

    @Override
    MethodSpec.Builder builderConstructor() {
      return super
          .builderConstructor()
          .addStatement(this.initTracker.initializeTracker(this.trackingFieldReference()));
    }

    @Override
    Builder builderBuildFor(Model model, BuilderStrategy strategy) {
      return super
          .builderBuildFor(model, strategy)
          .addException(this.initializationExceptionType());
    }

    @Override
    CodeBlock.Builder builderBuildBodyFor(Model model) {
      return CodeBlock
          .builder()
          .beginControlFlow(
              "if $L", this.initTracker.isAnyUninitialized(this.trackingFieldReference())
          )
          .add(this.determineWhichFieldsAreNotInitialized(model))
          .endControlFlow()
          .add(super.builderBuildBodyFor(model).build());
    }

    @Override
    CodeBlock.Builder builderSetterBodyFor(
        Attribute attribute,
        String paramName,
        String fieldName
    ) {
      return this.initTracker
          .markAttributeInitialized(this.trackingFieldReference(), attribute)
          .map(markStatement -> CodeBlock.builder().addStatement(markStatement))
          .orElseGet(CodeBlock::builder)
          .add(super.builderSetterBodyFor(attribute, paramName, fieldName).build());
    }

    private CodeBlock determineWhichFieldsAreNotInitialized(Model model) {
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
          .map(attr -> this
              .initTracker
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
        Attribute attribute
    ) {
      return CodeBlock
          .builder()
          .beginControlFlow("if $L", initializationCheck)
          .addStatement("$N.add($S)", UNINITIALIZED_SET_NAME, attribute.getIdentifier())
          .endControlFlow()
          .build();
    }

    private FieldSpec trackingField() {
      return FieldSpec
          .builder(this.initTracker.getTrackerType(), TRACKING_FIELD_NAME)
          .addModifiers(Modifier.PRIVATE, Modifier.TRANSIENT)
          .build();
    }

    private CodeBlock trackingFieldReference() {
      return CodeBlock.of("this.$N", TRACKING_FIELD_NAME);
    }

    private TypeName initializationExceptionType() {
      // TODO(ascopes): Customize this exception?
      return TypeName.get(IllegalStateException.class);
    }
  }
}
