package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.annotations.MutableModel;
import io.ascopes.katana.ap.settings.Setting;
import io.ascopes.katana.ap.settings.SettingsResolver;
import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import io.ascopes.katana.ap.utils.AnnotationUtils;
import io.ascopes.katana.ap.utils.Diagnostics;
import io.ascopes.katana.ap.utils.Logger;
import io.ascopes.katana.ap.utils.NamingUtils;
import io.ascopes.katana.ap.utils.Result;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
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

  private final SettingsResolver settingsResolver;
  private final MethodClassificationFactory methodClassifier;
  private final AttributeFactory attributeFactory;
  private final Diagnostics diagnostics;
  private final Elements elementUtils;
  private final Logger logger;

  public ModelFactory(
      SettingsResolver settingsResolver,
      MethodClassificationFactory methodClassifier,
      AttributeFactory attributeFactory,
      Diagnostics diagnostics,
      Elements elementUtils
  ) {
    this.settingsResolver = settingsResolver;
    this.methodClassifier = methodClassifier;
    this.attributeFactory = attributeFactory;
    this.elementUtils = elementUtils;
    this.diagnostics = diagnostics;
    this.logger = new Logger();
  }

  public Result<Model> create(
      TypeElement modelAnnotation,
      TypeElement annotatedElement
  ) {
    this.logger.debug(
        "Building model for {} annotated with {}",
        annotatedElement.getQualifiedName(),
        modelAnnotation
    );

    Result<Model> result = AnnotationUtils
        .findAnnotationMirror(annotatedElement, modelAnnotation)
        .ifOkFlatMap(mirror -> this.buildFor(annotatedElement, modelAnnotation, mirror));

    this.logger.debug("Model descriptor generation result = {}", result);
    return result;
  }

  private Result<Model> buildFor(
      TypeElement modelInterface,
      TypeElement modelAnnotation,
      AnnotationMirror annotationMirror
  ) {
    boolean mutable = this.isMutable(modelAnnotation);

    Model.Builder builder = Model
        .builder()
        .superInterface(modelInterface)
        .annotationMirror(annotationMirror)
        .mutable(mutable);

    return this.parseSettings(builder)
        .ifOkFlatMap(this::determineNames)
        .ifOkFlatMap(this::classifyMethods)
        .ifOkFlatMap(this::generateAttributes)
        .ifOkFlatMap(this::determineConstructors)
        .ifOkFlatMap(this::determineBuilders)
        .ifOkFlatMap(this::processModelLevelDeprecation)
        .ifOkMap(Model.Builder::build);
  }

  private boolean isMutable(TypeElement modelAnnotation) {
    return modelAnnotation
        .getSimpleName()
        .contentEquals(MutableModel.class.getSimpleName());
  }

  private Result<Model.Builder> parseSettings(Model.Builder builder) {
    return this.settingsResolver
        .parseSettings(
            builder.getSuperInterface(),
            builder.getAnnotationMirror(),
            builder.isMutable()
        )
        .ifOkMap(builder::settingsCollection);
  }

  private Result<Model.Builder> determineNames(Model.Builder builder) {
    Setting<String> packageNameSetting = builder
        .getSettingsCollection()
        .getPackageName();

    String packageName = packageNameSetting.getValue()
        .replace("*", this.elementUtils.getPackageOf(builder.getSuperInterface()).toString());

    try {
      NamingUtils.validatePackageName(packageName);
    } catch (IllegalArgumentException ex) {
      this.failIllegalPackageName(
          ex.getMessage(),
          packageNameSetting,
          packageNameSetting.getDeclaringElement().orElseGet(builder::getSuperInterface),
          packageNameSetting.getAnnotationMirror().orElseGet(builder::getAnnotationMirror),
          packageNameSetting.getAnnotationValue().orElse(null)
      );

      return Result.fail();
    }

    Setting<String> classNameSetting = builder
        .getSettingsCollection()
        .getClassName();

    String className = classNameSetting.getValue()
        .replace("*", builder.getSuperInterface().getSimpleName().toString());

    try {
      NamingUtils.validateClassName(className);
    } catch (IllegalArgumentException ex) {
      this.failIllegalClassName(
          ex.getMessage(),
          classNameSetting,
          classNameSetting.getDeclaringElement().orElseGet(builder::getSuperInterface),
          classNameSetting.getAnnotationMirror().orElseGet(builder::getAnnotationMirror),
          classNameSetting.getAnnotationValue().orElse(null)
      );

      return Result.fail();
    }

    String qualifiedName = packageName.isEmpty()
        ? className
        : String.join(".", packageName, className);

    builder
        .className(className)
        .packageName(packageName)
        .qualifiedName(qualifiedName);

    return Result.ok(builder);
  }

  private Result<Model.Builder> classifyMethods(Model.Builder builder) {
    return this.methodClassifier
        .create(builder.getSuperInterface(), builder.getSettingsCollection())
        .ifOkMap(builder::methods);
  }

  private Result<Model.Builder> processModelLevelDeprecation(Model.Builder builder) {
    TypeElement deprecatedAnnotation = this.elementUtils
        .getTypeElement(Deprecated.class.getCanonicalName());

    return AnnotationUtils
        .findAnnotationMirror(builder.getSuperInterface(), deprecatedAnnotation)
        .ifOkMap(builder::deprecatedAnnotation)
        .ifIgnoredReplace(builder);
  }

  private Result<Model.Builder> generateAttributes(Model.Builder builder) {
    return this.attributeFactory
        .create(builder.getMethods(), builder.getSettingsCollection())
        .ifOkMap(builder::attributes);
  }

  private Result<Model.Builder> determineConstructors(Model.Builder builder) {
    SettingsCollection settings = builder.getSettingsCollection();

    Set<Constructor> constructorSet = new HashSet<>();

    if (settings.getCopyConstructor().getValue()) {
      constructorSet.add(Constructor.COPY);
    }

    if (settings.getAllArgsConstructor().getValue()) {
      constructorSet.add(Constructor.ALL_ARGS);
    }

    if (settings.getDefaultArgsConstructor().getValue()) {
      // No-args constructor on an immutable type makes absolutely zero sense here.
      constructorSet.add(builder.isMutable() ? Constructor.NO_ARGS : Constructor.ALL_ARGS);
    }

    this.logger.trace("Will implement constructors: {}", constructorSet);
    builder.constructors(constructorSet);

    return Result.ok(builder);
  }

  private Result<Model.Builder> determineBuilders(Model.Builder builder) {
    // Edge case I probably won't account for.
    //
    // I wake up one day and decide "hey, lets make a model that has a builder, but also make it
    // so the model only has one field, which has the type of the builder".
    // Then I decide to also make an all-arguments constructor.
    //
    // There is literally zero reason anyone would reasonably do this, I think. If there is,
    // they can open an issue and explain it to me, and then I will probably go and scratch my
    // head over how to best deal with this for a few days, then alter the code in this method
    // to do something.... probably.
    SettingsCollection settings = builder.getSettingsCollection();

    if (settings.getBuilder().getValue()) {
      BuilderStrategy builderStrategy = BuilderStrategy
          .builder()
          .name(settings.getBuilderName().getValue())
          .toBuilderEnabled(settings.getToBuilder().getValue())
          .build();

      builder.builderStrategy(builderStrategy);
    }

    return Result.ok(builder);
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
}
