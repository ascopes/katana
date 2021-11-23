package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.annotations.internal.AttributeFeature;
import io.ascopes.katana.annotations.internal.ExclusionAdvice;
import io.ascopes.katana.annotations.internal.InclusionAdvice;
import io.ascopes.katana.ap.settings.Setting;
import io.ascopes.katana.ap.utils.AnnotationUtils;
import io.ascopes.katana.ap.utils.DiagnosticTemplates;
import io.ascopes.katana.ap.utils.Logger;
import io.ascopes.katana.ap.utils.Result;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.processing.Messager;
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
  private final DiagnosticTemplates diagnosticTemplates;
  private final Elements elementUtils;
  private final Messager messager;

  /**
   * @param messager            messager to report errors with.
   * @param elementUtils        element utilities to use.
   * @param diagnosticTemplates diagnostic templates to use for error messages.
   */
  public AttributeFeatureInclusionManager(
      DiagnosticTemplates diagnosticTemplates,
      Elements elementUtils,
      Messager messager
  ) {
    this.logger = new Logger();
    this.diagnosticTemplates = diagnosticTemplates;
    this.elementUtils = elementUtils;
    this.messager = messager;
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
      this.logger.debug(
          "Will explicitly exclude feature {} on attribute {}",
          featureClass.getCanonicalName(),
          getter
      );
      return Result.ok(false);
    }

    if (value.isExcludeAll() && hasAnnotation(getter, includeAnnotation)) {
      this.logger.debug(
          "Will explicitly include feature {} on attribute {}",
          featureClass.getCanonicalName(),
          getter
      );
      return Result.ok(true);
    }

    this.logger.debug("Using class-level inclusion mode for {}", getter);

    if (value.isIncludeAll()) {
      return Result.ok(true);
    }

    if (value.isExcludeAll()) {
      return Result.ok(false);
    }

    // Anything else isn't relevant here.
    return Result.ignore();
  }

  private void failCannotIncludeAndExclude(
      String attributeName,
      ExecutableElement getter,
      Class<? extends AttributeFeature> featureClass,
      Class<? extends Annotation> includeAnnotation,
      Class<? extends Annotation> excludeAnnotation
  ) {
    String message = this.diagnosticTemplates
        .template("includedAndExcluded")
        .placeholder("attributeName", attributeName)
        // TODO(ascopes): should I make this more specific somehow?
        .placeholder("category", featureClass.getSimpleName())
        .placeholder("getter", getter)
        .placeholder("includeAnnotation", includeAnnotation)
        .placeholder("excludeAnnotation", excludeAnnotation)
        .build();

    TypeElement annotationType = this.elementUtils
        .getTypeElement(includeAnnotation.getCanonicalName());

    @Nullable
    AnnotationMirror mirror = AnnotationUtils
        .findAnnotationMirror(getter, annotationType)
        .elseReturn(null);

    this.messager.printMessage(
        Kind.ERROR,
        message,
        getter,
        mirror
    );
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
