package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.annotations.internal.AttributeFeature;
import io.ascopes.katana.annotations.internal.ExclusionAdvice;
import io.ascopes.katana.annotations.internal.InclusionAdvice;
import io.ascopes.katana.ap.logging.Diagnostics;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.settings.Setting;
import io.ascopes.katana.ap.utils.AnnotationUtils;
import io.ascopes.katana.ap.utils.Result;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.function.Supplier;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Decision manager for deciding whether to include features on an attribute or not.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class AttributeFeatureInclusionManager {

  private final Logger logger;
  private final Diagnostics diagnostics;
  private final Elements elementUtils;

  /**
   * Initialize this manager.
   *
   * @param diagnostics compiler diagnostics to use.
   * @param elementUtils element utilities to use for introspection.
   */
  public AttributeFeatureInclusionManager(
      Diagnostics diagnostics,
      Elements elementUtils
  ) {
    this.logger = LoggerFactory.loggerFor(this.getClass());
    this.diagnostics = diagnostics;
    this.elementUtils = elementUtils;
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
  public <T extends AttributeFeature> Result<Boolean> check(
      String attributeName,
      Setting<T> setting,
      ExecutableElement getter
  ) {
    Class<T> featureClass = setting.getType();
    T value = setting.getValue();

    Class<? extends Annotation> includeAnnotation = Optional
        .ofNullable(featureClass.getAnnotation(InclusionAdvice.class))
        .map(InclusionAdvice::value)
        .orElseThrow(missingAnnotation(featureClass, InclusionAdvice.class));

    Class<? extends Annotation> excludeAnnotation = Optional
        .ofNullable(featureClass.getAnnotation(ExclusionAdvice.class))
        .map(ExclusionAdvice::value)
        .orElseThrow(missingAnnotation(featureClass, ExclusionAdvice.class));

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
    return Result.ignore();
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
        .elseReturn(null);

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

  private static boolean hasAnnotation(
      ExecutableElement getter,
      Class<? extends Annotation> annotation
  ) {
    return getter.getAnnotation(annotation) != null;
  }

  private static Supplier<? extends RuntimeException> missingAnnotation(
      Class<? extends AttributeFeature> featureClass,
      Class<? extends Annotation> missingAnnotation
  ) {
    return () -> new RuntimeException(
        "No @" + missingAnnotation.getCanonicalName()
            + " found on class " + featureClass.getCanonicalName()
    );
  }
}
