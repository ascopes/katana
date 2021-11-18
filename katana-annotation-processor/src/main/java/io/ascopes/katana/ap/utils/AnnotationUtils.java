package io.ascopes.katana.ap.utils;

import java.util.Map.Entry;
import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Various helpers for annotation processing.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public abstract class AnnotationUtils {

  private AnnotationUtils() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Find an annotation mirror for a given annotation class.
   *
   * @param annotatedElement the element to look for annotations on.
   * @param annotationType   the annotation class to look for.
   * @return a result holding the desired annotation mirror, or a failure result if not present.
   */
  public static Result<? extends AnnotationMirror> findAnnotationMirror(
      Element annotatedElement,
      TypeElement annotationType
  ) {
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
        .orElseGet(Result::fail);
  }

  /**
   * Get an attribute value from an annotation mirror if present.
   *
   * @param mirror the mirror to check.
   * @param name   the name of the attribute.
   * @return an optional holding the attribute value, or an empty optional if not present.
   */
  public static Optional<? extends AnnotationValue> getValue(
      AnnotationMirror mirror,
      String name
  ) {
    return mirror
        .getElementValues()
        .entrySet()
        .stream()
        .filter(entry -> entry.getKey().getSimpleName().contentEquals(name))
        .map(Entry::getValue)
        .findAny();
  }
}
