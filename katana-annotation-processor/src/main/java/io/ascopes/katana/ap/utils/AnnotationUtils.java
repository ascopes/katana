package io.ascopes.katana.ap.utils;

import java.util.Map.Entry;
import java.util.Objects;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.checkerframework.common.util.report.qual.ReportCreation;
import org.checkerframework.common.util.report.qual.ReportInherit;

/**
 * Various helpers for annotation processing.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@ReportCreation
@ReportInherit
public abstract class AnnotationUtils {

  private AnnotationUtils() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Find an annotation mirror for a given annotation class.
   *
   * @param annotatedElement the element to look for annotations on.
   * @param annotationType   the annotation class to look for.
   * @return a result holding the desired annotation mirror, or an ignored result if not found.
   */
  public static Result<? extends AnnotationMirror> findAnnotationMirror(
      Element annotatedElement,
      TypeElement annotationType
  ) {
    Objects.requireNonNull(annotatedElement, "annotatedElement was null");
    Objects.requireNonNull(annotationType, "annotationType was null");

    return annotatedElement
        .getAnnotationMirrors()
        .stream()
        .filter(mirror -> mirror
            .getAnnotationType()
            .asElement()
            .getSimpleName()
            .contentEquals(annotationType.getSimpleName()))
        .findAny()
        .map(Result::ok)
        .orElseGet(Result::ignore);
  }

  /**
   * Get an attribute value from an annotation mirror if present.
   *
   * @param mirror the mirror to check.
   * @param name   the name of the attribute.
   * @return an result holding the attribute value, or a failed result if not present.
   */
  public static Result<? extends AnnotationValue> getValue(
      AnnotationMirror mirror,
      String name
  ) {
    Objects.requireNonNull(mirror, "mirror was null");
    Objects.requireNonNull(name, "name was null");

    return mirror
        .getElementValues()
        .entrySet()
        .stream()
        .filter(entry -> entry.getKey().getSimpleName().contentEquals(name))
        .map(Entry::getValue)
        .findAny()
        .map(Result::ok)
        .orElseGet(Result::fail);
  }
}
