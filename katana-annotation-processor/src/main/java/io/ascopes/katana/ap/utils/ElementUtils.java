package io.ascopes.katana.ap.utils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Utilities for element processing.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public abstract class ElementUtils {
  private ElementUtils() {
    // Static-only class.
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Find the corresponding annotation mirror for a given annotation on a given annotated element.
   *
   * @param typeUtils        the type utilities to use for type introspection.
   * @param annotatedElement the element with the annotation applied.
   * @param annotationType   the annotation type to find the mirror for.
   * @return the mirror.
   * @throws IllegalStateException if the mirror was not present.
   */
  public static AnnotationMirror fetchAnnotationMirrorFor(
      Types typeUtils,
      Element annotatedElement,
      TypeElement annotationType
  ) throws IllegalStateException {
    return annotatedElement
        .getAnnotationMirrors()
        .stream()
        .filter(mirror -> typeUtils.isSameType(
            mirror.getAnnotationType(),
            annotationType.asType()
        ))
        .findAny()
        .orElseThrow(() -> new IllegalStateException(
            "Expected to find annotation mirror for " + annotationType + " on " + annotatedElement
        ));
  }

  /**
   * Get a reference to the unnamed root package that all other packages are contained within.
   *
   * @param elementUtils the compiler element utilities to use.
   * @return a reference to the unnamed package.
   */
  public static PackageElement fetchUnnamedPackage(Elements elementUtils) {
    return elementUtils.getPackageElement("");
  }
}
