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

package io.ascopes.katana.ap.types;

import io.ascopes.katana.annotations.Equality;
import io.ascopes.katana.annotations.Equality.CustomEquals;
import io.ascopes.katana.annotations.Equality.CustomHashCode;
import io.ascopes.katana.annotations.MutableModel;
import io.ascopes.katana.annotations.ToString;
import io.ascopes.katana.annotations.ToString.CustomToString;
import io.ascopes.katana.ap.attributes.AttributeDescriptor;
import io.ascopes.katana.ap.attributes.AttributeDescriptorFactory;
import io.ascopes.katana.ap.builders.BuilderStrategyFactory;
import io.ascopes.katana.ap.features.FeatureManager;
import io.ascopes.katana.ap.logging.Diagnostics;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.methods.EqualityStrategyDescriptor;
import io.ascopes.katana.ap.methods.EqualityStrategyDescriptor.CustomEqualityStrategyDescriptor;
import io.ascopes.katana.ap.methods.EqualityStrategyDescriptor.GeneratedEqualityStrategyDescriptor;
import io.ascopes.katana.ap.methods.MethodClassification;
import io.ascopes.katana.ap.methods.MethodClassificationFactory;
import io.ascopes.katana.ap.methods.ToStringStrategyDescriptor;
import io.ascopes.katana.ap.methods.ToStringStrategyDescriptor.CustomToStringStrategyDescriptor;
import io.ascopes.katana.ap.methods.ToStringStrategyDescriptor.GeneratedToStringStrategyDescriptor;
import io.ascopes.katana.ap.settings.SettingDescriptor;
import io.ascopes.katana.ap.settings.SettingsResolver;
import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import io.ascopes.katana.ap.types.ModelDescriptor.ModelDescriptorBuilder;
import io.ascopes.katana.ap.utils.AnnotationUtils;
import io.ascopes.katana.ap.utils.NamingUtils;
import io.ascopes.katana.ap.utils.Result;
import io.ascopes.katana.ap.utils.ResultCollector;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Mapper to read in the AST data from the compiler and produce meaningful information for other
 * components to follow to build the models later.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class ModelDescriptorFactory {

  private final Logger logger;
  private final Diagnostics diagnostics;
  private final Elements elementUtils;
  private final FeatureManager featureManager;
  private final SettingsResolver settingsResolver;
  private final MethodClassificationFactory methodClassifier;
  private final AttributeDescriptorFactory attributeDescriptorFactory;
  private final BuilderStrategyFactory builderStrategyFactory;

  /**
   * Initialize this factory.
   *
   * @param diagnostics  the diagnostics to report compilation errors with.
   * @param elementUtils the element utilities to use for introspection.
   * @param typeUtils    the type utilities to use for introspection.
   */
  public ModelDescriptorFactory(
      Diagnostics diagnostics,
      Elements elementUtils,
      Types typeUtils
  ) {
    this.logger = LoggerFactory.loggerFor(this.getClass());
    this.elementUtils = elementUtils;
    this.diagnostics = diagnostics;

    this.featureManager = new FeatureManager(diagnostics, elementUtils, typeUtils);
    this.settingsResolver = new SettingsResolver(elementUtils, typeUtils);
    this.methodClassifier = new MethodClassificationFactory(diagnostics, typeUtils);
    this.attributeDescriptorFactory = new AttributeDescriptorFactory(this.featureManager,
        elementUtils);
    this.builderStrategyFactory = new BuilderStrategyFactory();
  }

  /**
   * Create a model descriptor from a given model annotation and an annotated interface.
   *
   * @param annotationType the model annotation type to use (e.g. ImmutableModel or MutableModel).
   * @param interfaceType  the annotated interface to generate the model from.
   * @return the generated model descriptor, or an empty optional if an error occurred.
   */
  public Result<ModelDescriptor> create(
      TypeElement annotationType,
      TypeElement interfaceType
  ) {
    this.logger.debug(
        "Building model for {} annotated with {}",
        interfaceType.getQualifiedName(),
        annotationType
    );

    AnnotationMirror mirror = AnnotationUtils
        .findAnnotationMirror(interfaceType, annotationType)
        .orElseThrow(() -> new RuntimeException("Could not find annotation mirror"));

    boolean mutable = annotationType
        .getQualifiedName()
        .contentEquals(MutableModel.class.getCanonicalName());

    Result<ModelDescriptor> result = this.settingsResolver
        .parseSettings(interfaceType, mirror, mutable)
        .ifOkFlatMap(settings -> this.methodClassifier
            .create(interfaceType, settings)
            .ifOkMap(methodClassification -> new ModelCandidate(
                ModelDescriptor.builder(),
                interfaceType,
                settings,
                mirror,
                mutable,
                methodClassification
            ))
        )
        .ifOkCheck(this::setInterface)
        .ifOkCheck(this::setPackageName)
        .ifOkCheck(this::setClassName)
        .ifOkCheck(this::setDeprecation)
        .ifOkCheck(this::setSetterPrefix)
        .ifOkCheck(this::setConstructors)
        .ifOkCheck(this::setAttributes)
        .ifOkCheck(this::setBuilderStrategy)
        .ifOkCheck(this::setEqualityStrategy)
        .ifOkCheck(this::setToStringStrategy)
        .ifOkCheck(this::setIndent)
        .ifOkMap(ModelCandidate::getBuilder)
        .ifOkMap(ModelDescriptorBuilder::build);

    this.logger.debug("Model creation had result {}", result);

    return result;
  }

  private Result<Void> setInterface(ModelCandidate candidate) {
    this.logger.trace("Setting interface for candidate");

    candidate
        .getBuilder()
        .superInterface(candidate.getInterfaceType());

    return Result.ok();
  }

  private Result<Void> setPackageName(ModelCandidate candidate) {
    this.logger.trace("Setting package name for candidate");

    SettingDescriptor<String> packageNameSettingDescriptor = candidate
        .getSettings()
        .getPackageName();

    String packageName = packageNameSettingDescriptor.getValue().replace(
        "*",
        this.elementUtils.getPackageOf(candidate.getInterfaceType()).toString()
    );

    try {
      NamingUtils.validatePackageName(packageName);
    } catch (IllegalArgumentException ex) {
      this.failIllegalPackageName(
          ex.getMessage(),
          packageNameSettingDescriptor,
          packageNameSettingDescriptor.getDeclaringElement().orElseGet(candidate::getInterfaceType),
          packageNameSettingDescriptor.getAnnotationMirror().orElseGet(candidate::getMirror),
          packageNameSettingDescriptor.getAnnotationValue().orElse(null)
      );

      return Result.fail("Illegal package name pattern");
    }

    candidate
        .getBuilder()
        .packageName(packageName);

    return Result.ok();
  }

  private Result<Void> setClassName(ModelCandidate candidate) {
    this.logger.trace("Setting class name for candidate");

    SettingDescriptor<String> classNameSettingDescriptor = candidate.getSettings().getClassName();

    String className = classNameSettingDescriptor.getValue()
        .replace("*", candidate.getInterfaceType().getSimpleName().toString());

    try {
      NamingUtils.validateClassName(className);
    } catch (IllegalArgumentException ex) {
      this.failIllegalClassName(
          ex.getMessage(),
          classNameSettingDescriptor,
          classNameSettingDescriptor.getDeclaringElement().orElseGet(candidate::getInterfaceType),
          classNameSettingDescriptor.getAnnotationMirror().orElseGet(candidate::getMirror),
          classNameSettingDescriptor.getAnnotationValue().orElse(null)
      );

      return Result.fail("Illegal class name pattern");
    }

    candidate
        .getBuilder()
        .className(className);

    return Result.ok();
  }

  private Result<Void> setDeprecation(ModelCandidate candidate) {
    this.logger.trace("Setting deprecation state for candidate");

    TypeElement deprecatedAnnotation = this.elementUtils
        .getTypeElement(Deprecated.class.getCanonicalName());

    AnnotationUtils
        .findAnnotationMirror(candidate.getInterfaceType(), deprecatedAnnotation)
        .ifPresent(candidate.getBuilder()::deprecatedAnnotation);

    return Result.ok();
  }

  private Result<Void> setSetterPrefix(ModelCandidate candidate) {
    // TODO(ascopes): Validate this?
    String prefix = candidate
        .getSettings()
        .getSetterPrefix()
        .getValue();

    candidate
        .getBuilder()
        .setterPrefix(prefix);

    return Result.ok();
  }

  private Result<Void> setAttributes(ModelCandidate candidate) {
    this.logger.trace("Setting attributes for candidate");

    Result<List<AttributeDescriptor>> result = this.attributeDescriptorFactory
        .create(candidate.getMethods(), candidate.getSettings(), candidate.isMutable())
        .collect(ResultCollector.aggregating(Collectors.toList()));

    if (result.isFailed()) {
      return Result.fail(result);
    }

    for (AttributeDescriptor attributeDescriptor : result.unwrap()) {
      this.logger.trace("Adding attribute {} to model", attributeDescriptor);
      candidate.getBuilder().attribute(attributeDescriptor);
    }

    return Result.ok();
  }

  private Result<Void> setConstructors(ModelCandidate candidate) {
    this.logger.trace("Setting constructors for candidate");

    SettingsCollection settings = candidate.getSettings();
    ModelDescriptorBuilder modelDescriptorBuilder = candidate.getBuilder();

    if (settings.getCopyConstructor().getValue()) {
      this.addConstructor(modelDescriptorBuilder, Constructor.COPY);
    }

    if (settings.getAllArgsConstructor().getValue()) {
      this.addConstructor(modelDescriptorBuilder, Constructor.ALL_ARGS);
    }

    if (settings.getDefaultArgsConstructor().getValue()) {
      // No-args constructor on an immutable type makes absolutely zero sense here.
      Constructor constructor = candidate.isMutable()
          ? Constructor.NO_ARGS
          : Constructor.ALL_ARGS;

      this.addConstructor(modelDescriptorBuilder, constructor);
    }

    return Result.ok();
  }

  private void addConstructor(ModelDescriptorBuilder modelDescriptorBuilder,
      Constructor constructor) {
    this.logger.trace("Will implement {} constructor", constructor);
    modelDescriptorBuilder.constructor(constructor);
  }

  private Result<Void> setBuilderStrategy(ModelCandidate candidate) {
    this.logger.trace("Setting builder strategy for candidate");

    this.builderStrategyFactory
        .create(candidate.getSettings())
        .ifPresent(candidate.getBuilder()::builderStrategy);

    return Result.ok();
  }

  private Result<Void> setEqualityStrategy(ModelCandidate candidate) {
    this.logger.trace("Setting equality strategy for candidate");

    @Nullable
    EqualityStrategyDescriptor equalityStrategy;

    switch (candidate.getSettings().getEqualityMode().getValue()) {
      case INCLUDE_ALL:
        this.logger.trace("Using all-inclusive generated equality strategy");
        equalityStrategy = new GeneratedEqualityStrategyDescriptor(true);
        break;

      case EXCLUDE_ALL:
        this.logger.trace("Using all-exclusive generated equality strategy");
        equalityStrategy = new GeneratedEqualityStrategyDescriptor(false);
        break;

      case CUSTOM: {
        this.logger.trace("Using custom equality strategy");
        Result<ExecutableElement> equalsMethod = this.featureManager
            .getRequiredCustomMethod(
                candidate.getInterfaceType(),
                Equality.class,
                CustomEquals.class,
                candidate.getMethods()
            );

        Result<ExecutableElement> hashCodeMethod = this.featureManager
            .getRequiredCustomMethod(
                candidate.getInterfaceType(),
                Equality.class,
                CustomHashCode.class,
                candidate.getMethods()
            );

        if (equalsMethod.isFailed()) {
          return Result.fail(equalsMethod);
        }

        if (hashCodeMethod.isFailed()) {
          return Result.fail(hashCodeMethod);
        }

        equalityStrategy = new CustomEqualityStrategyDescriptor(
            equalsMethod.unwrap(),
            hashCodeMethod.unwrap()
        );

        break;
      }

      default:
        this.logger.trace("Using no equality strategy");
        equalityStrategy = null;
        break;
    }

    candidate
        .getBuilder()
        .equalityStrategy(equalityStrategy);

    return Result.ok();
  }

  private Result<Void> setToStringStrategy(ModelCandidate candidate) {
    this.logger.trace("Setting toString strategy for candidate");

    ToStringStrategyDescriptor toStringStrategy;

    switch (candidate.getSettings().getToStringMode().getValue()) {
      case INCLUDE_ALL:
        this.logger.trace("Using all-inclusive generated toString strategy");
        toStringStrategy = new GeneratedToStringStrategyDescriptor(true);
        break;

      case EXCLUDE_ALL:
        this.logger.trace("Using all-exclusive generated toString strategy");
        toStringStrategy = new GeneratedToStringStrategyDescriptor(false);
        break;

      case CUSTOM: {
        this.logger.trace("Using custom toString strategy");

        Result<ExecutableElement> toStringMethod = this.featureManager
            .getRequiredCustomMethod(
                candidate.getInterfaceType(),
                ToString.class,
                CustomToString.class,
                candidate.getMethods()
            );

        if (toStringMethod.isFailed()) {
          return Result.fail(toStringMethod);
        }

        toStringStrategy = new CustomToStringStrategyDescriptor(toStringMethod.unwrap());
        break;
      }

      default:
        this.logger.trace("Using no toString strategy");
        toStringStrategy = null;
        break;
    }

    candidate
        .getBuilder()
        .toStringStrategy(toStringStrategy);

    return Result.ok();
  }

  private Result<Void> setIndent(ModelCandidate candidate) {
    this.logger.trace("Setting indent for candidate");

    candidate
        .getBuilder()
        .indent(candidate.getSettings().getIndent().getValue());

    return Result.ok();
  }

  private void failIllegalPackageName(
      String message,
      SettingDescriptor<?> packageNameSettingDescriptor,
      @Nullable Element element,
      @Nullable AnnotationMirror annotationMirror,
      @Nullable AnnotationValue annotationValue
  ) {
    this.diagnostics
        .builder()
        .kind(Kind.ERROR)
        .element(element)
        .annotationMirror(annotationMirror)
        .annotationValue(annotationValue)
        .template("illegalPackageName")
        .param("message", message)
        .param("settingDescription", packageNameSettingDescriptor.getDescription())
        .log();
  }

  private void failIllegalClassName(
      String message,
      SettingDescriptor<?> classNameSettingDescriptor,
      @Nullable Element element,
      @Nullable AnnotationMirror annotationMirror,
      @Nullable AnnotationValue annotationValue
  ) {
    this.diagnostics
        .builder()
        .kind(Kind.ERROR)
        .element(element)
        .annotationMirror(annotationMirror)
        .annotationValue(annotationValue)
        .template("illegalClassName")
        .param("message", message)
        .param("settingDescription", classNameSettingDescriptor.getDescription())
        .log();
  }

  /**
   * Wrapper around the initial information we have about a model. Used in the process of building
   * an actual model.
   *
   * @author Ashley Scopes
   * @since 0.0.1
   */
  private static final class ModelCandidate {

    private final ModelDescriptorBuilder modelDescriptorBuilder;
    private final TypeElement interfaceType;
    private final SettingsCollection settings;
    private final AnnotationMirror mirror;
    private final boolean mutable;
    private final MethodClassification methods;

    public ModelCandidate(
        ModelDescriptorBuilder modelDescriptorBuilder,
        TypeElement interfaceType,
        SettingsCollection settings,
        AnnotationMirror mirror,
        boolean mutable,
        MethodClassification methods
    ) {
      this.modelDescriptorBuilder = Objects.requireNonNull(modelDescriptorBuilder);
      this.interfaceType = Objects.requireNonNull(interfaceType);
      this.settings = Objects.requireNonNull(settings);
      this.mirror = Objects.requireNonNull(mirror);
      this.mutable = mutable;
      this.methods = Objects.requireNonNull(methods);
    }

    public ModelDescriptorBuilder getBuilder() {
      return this.modelDescriptorBuilder;
    }

    public TypeElement getInterfaceType() {
      return this.interfaceType;
    }

    public SettingsCollection getSettings() {
      return this.settings;
    }

    public AnnotationMirror getMirror() {
      return this.mirror;
    }

    public boolean isMutable() {
      return this.mutable;
    }

    public MethodClassification getMethods() {
      return this.methods;
    }
  }
}
