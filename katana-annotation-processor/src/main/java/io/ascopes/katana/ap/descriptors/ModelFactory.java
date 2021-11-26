package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.annotations.MutableModel;
import io.ascopes.katana.ap.settings.Setting;
import io.ascopes.katana.ap.settings.SettingsResolver;
import io.ascopes.katana.ap.utils.AnnotationUtils;
import io.ascopes.katana.ap.utils.Diagnostics;
import io.ascopes.katana.ap.utils.Logger;
import io.ascopes.katana.ap.utils.NamingUtils;
import io.ascopes.katana.ap.utils.Result;
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
        .ifOkFlatMap(this::determinePackageName)
        .ifOkFlatMap(this::determineClassName)
        .ifOkFlatMap(this::classifyMethods)
        .ifOkFlatMap(this::generateAttributes)
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

  private Result<Model.Builder> determinePackageName(Model.Builder builder) {
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

    return Result
        .ok(packageName)
        .ifOkMap(builder::packageName);
  }

  private Result<Model.Builder> determineClassName(Model.Builder builder) {
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

    return Result
        .ok(className)
        .ifOkMap(builder::className);
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
