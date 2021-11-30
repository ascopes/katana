package io.ascopes.katana.ap.utils;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
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
public final class AnnotationUtils {

  private AnnotationUtils() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Find an annotation mirror for the given annotation type on the given annotated element.
   *
   * @param annotatedElement the element to look for annotations on.
   * @param annotationType   the annotation type to look for annotations that match.
   * @return an optional containing the result if found.
   */
  public static Optional<? extends AnnotationMirror> findAnnotationMirror(
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
        .findAny();
  }

  /**
   * Get a value for a given attribute from a given annotation mirror.
   *
   * <p>This ignores implicit default values. Only explicitly provided information will be
   * considered here.
   *
   * @param mirror the mirror to look at.
   * @param name   the name of the attribute.
   * @return the value of the attribute in an optional, if the attribute is present.
   */
  public static Optional<? extends AnnotationValue> getValue(
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
        .findAny();
  }
}
