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
