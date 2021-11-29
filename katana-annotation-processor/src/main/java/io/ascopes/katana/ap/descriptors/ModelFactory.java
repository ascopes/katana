package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.annotations.Equality;
import io.ascopes.katana.annotations.Equality.CustomEquals;
import io.ascopes.katana.annotations.Equality.CustomHashCode;
import io.ascopes.katana.annotations.MutableModel;
import io.ascopes.katana.ap.descriptors.EqualityStrategy.CustomEqualityStrategy;
import io.ascopes.katana.ap.descriptors.Model.Builder;
import io.ascopes.katana.ap.logging.Diagnostics;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.settings.Setting;
import io.ascopes.katana.ap.settings.SettingsResolver;
import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import io.ascopes.katana.ap.utils.AnnotationUtils;
import io.ascopes.katana.ap.utils.NamingUtils;
import io.ascopes.katana.ap.utils.Result;
import io.ascopes.katana.ap.utils.ResultCollector;
import java.util.Objects;
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
public final class ModelFactory {

  private final Logger logger;
  private final Diagnostics diagnostics;
  private final Elements elementUtils;
  private final FeatureManager featureManager;
  private final SettingsResolver settingsResolver;
  private final MethodClassificationFactory methodClassifier;
  private final AttributeFactory attributeFactory;
  private final BuilderStrategyFactory builderStrategyFactory;

  /**
   * Initialize this factory.
   *
   * @param diagnostics  the diagnostics to report compilation errors with.
   * @param elementUtils the element utilities to use for introspection.
   * @param typeUtils    the type utilities to use for introspection.
   */
  public ModelFactory(
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
    this.attributeFactory = new AttributeFactory(diagnostics, this.featureManager, elementUtils);
    this.builderStrategyFactory = new BuilderStrategyFactory();
  }

  /**
   * Create a model descriptor from a given model annotation and an annotated interface.
   *
   * @param annotationType the model annotation type to use (e.g. ImmutableModel or MutableModel).
   * @param interfaceType  the annotated interface to generate the model from.
   * @return the generated model descriptor, or an empty optional if an error occurred.
   */
  public Result<Model> create(
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
        .unwrap();

    boolean mutable = annotationType
        .getQualifiedName()
        .contentEquals(MutableModel.class.getCanonicalName());

    return this.settingsResolver
        .parseSettings(interfaceType, mirror, mutable)
        .ifOkFlatMap(settings -> this.methodClassifier
            .create(interfaceType, settings)
            .ifOkMap(methodClassification -> new ModelCandidate(
                Model.builder(),
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
        .ifOkCheck(this::setIndent)
        .ifOkMap(ModelCandidate::getBuilder)
        .ifOkMap(Builder::build);
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

    Setting<String> packageNameSetting = candidate.getSettings().getPackageName();

    String packageName = packageNameSetting.getValue()
        .replace("*", this.elementUtils.getPackageOf(candidate.getInterfaceType()).toString());

    try {
      NamingUtils.validatePackageName(packageName);
    } catch (IllegalArgumentException ex) {
      this.failIllegalPackageName(
          ex.getMessage(),
          packageNameSetting,
          packageNameSetting.getDeclaringElement().orElseGet(candidate::getInterfaceType),
          packageNameSetting.getAnnotationMirror().orElseGet(candidate::getMirror),
          packageNameSetting.getAnnotationValue().orElse(null)
      );

      return Result.fail();
    }

    candidate
        .getBuilder()
        .packageName(packageName);

    return Result.ok();
  }

  private Result<Void> setClassName(ModelCandidate candidate) {
    this.logger.trace("Setting class name for candidate");

    Setting<String> classNameSetting = candidate.getSettings().getClassName();

    String className = classNameSetting.getValue()
        .replace("*", candidate.getInterfaceType().getSimpleName().toString());

    try {
      NamingUtils.validateClassName(className);
    } catch (IllegalArgumentException ex) {
      this.failIllegalClassName(
          ex.getMessage(),
          classNameSetting,
          classNameSetting.getDeclaringElement().orElseGet(candidate::getInterfaceType),
          classNameSetting.getAnnotationMirror().orElseGet(candidate::getMirror),
          classNameSetting.getAnnotationValue().orElse(null)
      );

      return Result.fail();
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
        .ifOkMap(candidate.getBuilder()::deprecatedAnnotation);

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

    return this.attributeFactory
        .create(candidate.getMethodClassification(), candidate.getSettings(), candidate.isMutable())
        .peek(attr -> attr.ifOk(candidate.getBuilder()::attribute))
        .collect(ResultCollector.discarding())
        .dropValue();
  }

  private Result<Void> setConstructors(ModelCandidate candidate) {
    this.logger.trace("Setting constructors for candidate");

    SettingsCollection settings = candidate.getSettings();
    Builder builder = candidate.getBuilder();

    if (settings.getCopyConstructor().getValue()) {
      this.addConstructor(builder, Constructor.COPY);
    }

    if (settings.getAllArgsConstructor().getValue()) {
      this.addConstructor(builder, Constructor.ALL_ARGS);
    }

    if (settings.getDefaultArgsConstructor().getValue()) {
      // No-args constructor on an immutable type makes absolutely zero sense here.
      Constructor constructor = candidate.isMutable()
          ? Constructor.NO_ARGS
          : Constructor.ALL_ARGS;

      this.addConstructor(builder, constructor);
    }

    return Result.ok();
  }

  private void addConstructor(Model.Builder builder, Constructor constructor) {
    this.logger.trace("Will implement {} constructor", constructor);
    builder.constructor(constructor);
  }

  private Result<Void> setBuilderStrategy(ModelCandidate candidate) {
    this.logger.trace("Setting builder strategy for candidate");

    return this.builderStrategyFactory
        .create(candidate.getSettings())
        .ifOk(candidate.getBuilder()::builderStrategy)
        .dropValue();
  }

  private Result<Void> setEqualityStrategy(ModelCandidate candidate) {
    this.logger.trace("Setting equality strategy for candidate");

    switch (candidate.getSettings().getEqualityMode().getValue()) {
      case INCLUDE_ALL:
        candidate
            .getBuilder()
            .equalityStrategy(new EqualityStrategy.GeneratedEqualityStrategy(true));
        return Result.ok();

      case EXCLUDE_ALL:
        candidate
            .getBuilder()
            .equalityStrategy(new EqualityStrategy.GeneratedEqualityStrategy(false));
        return Result.ok();

      case DISABLE:
        candidate.getBuilder().equalityStrategy(null);
        return Result.ok();
    }

    Result<ExecutableElement> equalsMethod = this.featureManager
        .getRequiredCustomMethod(
            candidate.getInterfaceType(),
            Equality.class,
            CustomEquals.class,
            candidate.getMethodClassification()
        );

    Result<ExecutableElement> hashCodeMethod = this.featureManager
        .getRequiredCustomMethod(
            candidate.getInterfaceType(),
            Equality.class,
            CustomHashCode.class,
            candidate.getMethodClassification()
        );

    if (equalsMethod.isNotOk() || hashCodeMethod.isNotOk()) {
      return Result.fail();
    }

    EqualityStrategy strategy = new CustomEqualityStrategy(
        equalsMethod.unwrap(),
        hashCodeMethod.unwrap()
    );

    candidate.getBuilder().equalityStrategy(strategy);

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
      Setting<?> packageNameSetting,
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
        .param("settingDescription", packageNameSetting.getDescription())
        .log();
  }

  private void failIllegalClassName(
      String message,
      Setting<?> classNameSetting,
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
        .param("settingDescription", classNameSetting.getDescription())
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

    private final Builder builder;
    private final TypeElement interfaceType;
    private final SettingsCollection settings;
    private final AnnotationMirror mirror;
    private final boolean mutable;
    private final MethodClassification methodClassification;

    public ModelCandidate(
        Builder builder,
        TypeElement interfaceType,
        SettingsCollection settings,
        AnnotationMirror mirror,
        boolean mutable,
        MethodClassification methodClassification
    ) {
      this.builder = Objects.requireNonNull(builder);
      this.interfaceType = Objects.requireNonNull(interfaceType);
      this.settings = Objects.requireNonNull(settings);
      this.mirror = Objects.requireNonNull(mirror);
      this.mutable = mutable;
      this.methodClassification = Objects.requireNonNull(methodClassification);
    }

    public Builder getBuilder() {
      return this.builder;
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

    public MethodClassification getMethodClassification() {
      return this.methodClassification;
    }
  }
}
