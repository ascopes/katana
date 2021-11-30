package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.annotations.advices.CustomMethodAdvice;
import io.ascopes.katana.annotations.advices.CustomMethodAdvice.This;
import io.ascopes.katana.annotations.advices.CustomMethodAdvices;
import io.ascopes.katana.annotations.advices.ExclusionAdvice;
import io.ascopes.katana.annotations.advices.InclusionAdvice;
import io.ascopes.katana.annotations.features.AttributeFeature;
import io.ascopes.katana.annotations.features.CustomizableAttributeFeature;
import io.ascopes.katana.ap.logging.Diagnostics;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.settings.Setting;
import io.ascopes.katana.ap.utils.AnnotationUtils;
import io.ascopes.katana.ap.utils.Result;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Decision manager for deciding whether to include features on an attribute or not.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class FeatureManager {

  private final Logger logger;
  private final Diagnostics diagnostics;
  private final Elements elementUtils;
  private final Types typeUtils;

  /**
   * Initialize this manager.
   *
   * @param diagnostics  compiler diagnostics to use.
   * @param elementUtils element utilities to use for introspection.
   * @param typeUtils    type utilities to use for introspection.
   */
  public FeatureManager(
      Diagnostics diagnostics,
      Elements elementUtils,
      Types typeUtils
  ) {
    this.logger = LoggerFactory.loggerFor(this.getClass());
    this.diagnostics = diagnostics;
    this.elementUtils = elementUtils;
    this.typeUtils = typeUtils;
  }

  /**
   * Determine whether to include a feature for a given setting applied on the type level, and the
   * getter for an attribute.
   *
   * @param attributeName the attribute name.
   * @param setting       the setting to control inclusion policy for the feature.
   * @param getter        the getter that may have a custom inclusion annotation on it.
   * @param <T>           the type of the feature.
   * @return the inclusion result, or a failure if something was unable to be extracted.
   */
  public <T extends AttributeFeature> Result<Boolean> checkInclusion(
      String attributeName,
      Setting<T> setting,
      ExecutableElement getter
  ) {
    Class<T> featureClass = setting.getType();
    T value = setting.getValue();

    Class<? extends Annotation> includeAnnotation = Optional
        .ofNullable(featureClass.getAnnotation(InclusionAdvice.class))
        .map(InclusionAdvice::annotation)
        .orElseThrow(() -> missingAnnotation(featureClass, InclusionAdvice.class));

    Class<? extends Annotation> excludeAnnotation = Optional
        .ofNullable(featureClass.getAnnotation(ExclusionAdvice.class))
        .map(ExclusionAdvice::annotation)
        .orElseThrow(() -> missingAnnotation(featureClass, ExclusionAdvice.class));

    if (value.isIncludeAll() && value.isExcludeAll()) {
      this.failCannotIncludeAndExclude(
          attributeName,
          getter,
          featureClass,
          includeAnnotation,
          excludeAnnotation
      );
      return Result.fail();
    }

    if (value.isIncludeAll() && hasAnnotation(getter, excludeAnnotation)) {
      this.logger.trace(
          "Will explicitly exclude feature {} on attribute {}",
          featureClass.getCanonicalName(),
          getter
      );
      return Result.ok(false);
    }

    if (value.isExcludeAll() && hasAnnotation(getter, includeAnnotation)) {
      this.logger.trace(
          "Will explicitly include feature {} on attribute {}",
          featureClass.getCanonicalName(),
          getter
      );
      return Result.ok(true);
    }

    if (value.isIncludeAll()) {
      this.logger.trace(
          "Using class-level inclusion mode to include {} for feature {}",
          getter,
          featureClass.getCanonicalName()
      );
      return Result.ok(true);
    }

    if (value.isExcludeAll()) {
      this.logger.trace(
          "Using class-level inclusion mode to exclude {} from feature {}",
          getter,
          featureClass.getCanonicalName()
      );
      return Result.ok(false);
    }

    // Anything else isn't relevant here.
    this.logger.trace(
        "{} is being ignored for feature {}",
        getter,
        featureClass.getCanonicalName()
    );
    return Result.ok(false);
  }

  /**
   * Find the implementation of the custom method for the given feature type and custom method
   * annotation.
   *
   * @param interfaceType    the interface that the model is defined with.
   * @param featureType      the feature type to look for.
   * @param methodAnnotation the method annotation to look for.
   * @param knownMethods     the known methods to operate on.
   * @return an OK result if found, or a failure if not found or if something was not right.
   */
  public Result<ExecutableElement> getRequiredCustomMethod(
      TypeElement interfaceType,
      Class<? extends CustomizableAttributeFeature> featureType,
      Class<? extends Annotation> methodAnnotation,
      MethodClassification knownMethods
  ) {
    CustomMethodAdvice advice = this
        .getCustomMethodAdviceFor(featureType, methodAnnotation);

    return this.getCorrectCustomMethod(interfaceType, knownMethods, methodAnnotation, advice);
  }

  private CustomMethodAdvice getCustomMethodAdviceFor(
      Class<? extends CustomizableAttributeFeature> featureType,
      Class<? extends Annotation> methodAnnotation
  ) {
    // We get this info first, as it is metadata on the feature class that tells us the signature
    // that the method annotated with methodAnnotation needs to have.
    CustomMethodAdvice customMethodAdvice = this.advicesForFeature(featureType)
        .filter(advice -> advice.annotation().equals(methodAnnotation))
        .findFirst()
        .orElseThrow(() -> missingCustomMethodAdvice(featureType, methodAnnotation));

    this.logger.trace(
        "Found method advice {} defining signature for method annotation {} as {} -> {}",
        customMethodAdvice,
        methodAnnotation.getCanonicalName(),
        customMethodAdvice.returns(),
        customMethodAdvice.consumes()
    );

    return customMethodAdvice;
  }

  private Stream<CustomMethodAdvice> advicesForFeature(
      Class<? extends CustomizableAttributeFeature> featureClass
  ) {
    // The annotation is repeatable, so may be replaced with the repeated wrapper annotation
    // internally if more than one is present.
    if (featureClass.isAnnotationPresent(CustomMethodAdvice.class)) {
      CustomMethodAdvice advice = featureClass.getAnnotation(CustomMethodAdvice.class);
      this.logger.trace("Got advice for {} from {}", featureClass, advice);
      return Stream.of();
    }

    CustomMethodAdvices advices = featureClass.getAnnotation(CustomMethodAdvices.class);

    if (advices != null && advices.value().length > 0) {
      this.logger.trace("Got advices for {} from {}", featureClass, advices);
      return Stream.of(advices.value());
    }

    throw missingAnnotation(featureClass, CustomMethodAdvices.class, CustomMethodAdvice.class);
  }

  private Result<ExecutableElement> getCorrectCustomMethod(
      TypeElement interfaceType,
      MethodClassification knownMethods,
      Class<? extends Annotation> methodAnnotationType,
      CustomMethodAdvice advice
  ) {
    // Find all methods annotated with the method annotation.
    List<ExecutableElement> candidateMethods = knownMethods
        .getStaticMethods()
        .stream()
        .filter(method -> method.getAnnotation(methodAnnotationType) != null)
        .collect(Collectors.toList());

    if (candidateMethods.isEmpty()) {
      this.failNoCustomMethodFound(interfaceType, advice);
      // Don't bother processing further, other stuff might break.
      return Result.fail();
    }

    // Guaranteed to be available.
    ExecutableElement method = candidateMethods.get(0);

    if (!this.isSubSignature(advice, method)) {
      this.failInvalidCustomSignature(method, advice);
      return Result.fail();
    }

    return Result.ok(method);
  }

  private boolean isSubSignature(CustomMethodAdvice advice, ExecutableElement method) {
    assert method.getAnnotation(advice.annotation()) != null;
    TypeElement enclosingType = enclosingType(method);

    if (advice.consumes().length != method.getParameters().size()) {
      return false;
    }

    TypeMirror adviceReturnMirror = this.getType(enclosingType, advice.returns());

    // I am ill and I cant wrap my head around whether this is the right way around or not.
    // Probably got in and out directionality mixed up.
    // TODO: revisit this to ensure it is correct.
    if (!this.typeUtils.isAssignable(method.getReturnType(), adviceReturnMirror)) {
      return false;
    }

    for (int i = 0; i < advice.consumes().length; ++i) {
      Class<?> adviceParam = advice.consumes()[i];
      TypeMirror adviceParamMirror = this.getType(enclosingType, adviceParam);
      TypeMirror methodParamMirror = method.getParameters().get(i).asType();
      if (!this.typeUtils.isAssignable(methodParamMirror, adviceParamMirror)) {
        return false;
      }
    }

    return true;
  }

  private TypeMirror getType(TypeElement interfaceType, Class<?> type) {
    if (type.equals(This.class)) {
      return interfaceType.asType();
    }

    if (type.equals(void.class)) {
      return this.typeUtils.getNoType(TypeKind.VOID);
    }

    if (type.isArray()) {
      TypeMirror componentType = this.getType(interfaceType, type.getComponentType());
      return this.typeUtils.getArrayType(componentType);
    }

    if (type.isPrimitive()) {
      String name = type.getSimpleName().toUpperCase(Locale.ROOT);
      TypeKind kind = TypeKind.valueOf(name);
      return this.typeUtils.getPrimitiveType(kind);
    }

    TypeElement element = this.elementUtils.getTypeElement(type.getCanonicalName());
    return element.asType();
  }

  private void failCannotIncludeAndExclude(
      String attributeName,
      ExecutableElement getter,
      Class<? extends AttributeFeature> featureClass,
      Class<? extends Annotation> includeAnnotation,
      Class<? extends Annotation> excludeAnnotation
  ) {
    TypeElement annotationType = this.elementUtils
        .getTypeElement(includeAnnotation.getCanonicalName());

    @Nullable
    AnnotationMirror mirror = AnnotationUtils
        .findAnnotationMirror(getter, annotationType)
        .orElse(null);

    this.diagnostics
        .builder()
        .kind(Kind.ERROR)
        .element(getter)
        .annotationMirror(mirror)
        .template("includedAndExcluded")
        .param("attributeName", attributeName)
        // TODO(ascopes): should I make this more specific somehow?
        .param("category", featureClass.getSimpleName())
        .param("getter", getter)
        .param("includeAnnotation", includeAnnotation)
        .param("excludeAnnotation", excludeAnnotation)
        .log();
  }

  private void failInvalidCustomSignature(
      ExecutableElement method,
      CustomMethodAdvice advice
  ) {
    // TODO(ascopes): implement this error message.
    this.diagnostics
        .builder()
        .kind(Kind.ERROR)
        .element(method)
        .template("invalidCustomSignature")
        .param("method", method.toString())
        .param("expectedReturnType", advice.returns().getCanonicalName())
        .param("expectedParamsTypes", advice.annotation().getCanonicalName())
        .log();
  }

  private void failNoCustomMethodFound(TypeElement interfaceType, CustomMethodAdvice advice) {
    // TODO(ascopes): implement this error message.
    this.diagnostics
        .builder()
        .kind(Kind.ERROR)
        .element(interfaceType)
        .template("noCustomMethodFound")
        .param("expectedReturnType", advice.returns().getCanonicalName())
        .param("expectedParamsTypes", advice.annotation().getCanonicalName())
        .log();
  }

  private static boolean hasAnnotation(
      ExecutableElement getter,
      Class<? extends Annotation> annotation
  ) {
    return getter.getAnnotation(annotation) != null;
  }

  private static TypeElement enclosingType(ExecutableElement method) {
    return (TypeElement) method.getEnclosingElement();
  }

  @SafeVarargs
  private static RuntimeException missingAnnotation(
      Class<? extends AttributeFeature> featureClass,
      Class<? extends Annotation>... missingAnnotations
  ) {
    String annotationsList = Stream
        .of(missingAnnotations)
        .map(annotation -> "@" + annotation.getCanonicalName())
        .collect(Collectors.joining(", ", "[", "]"));

    // This is a library issue if this occurs.
    return new RuntimeException(
        "No annotations in the list "
            + annotationsList
            + " found on class "
            + featureClass.getCanonicalName()
    );
  }

  private static RuntimeException missingCustomMethodAdvice(
      Class<? extends CustomizableAttributeFeature> featureClass,
      Class<? extends Annotation> missingAdviceAnnotationType
  ) {
    // This is a library issue if this occurs.
    return new RuntimeException(
        "No advice for "
            + missingAdviceAnnotationType.getCanonicalName()
            + " found on feature "
            + featureClass.getCanonicalName()
    );
  }
}
