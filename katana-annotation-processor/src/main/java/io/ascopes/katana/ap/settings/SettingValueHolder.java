package io.ascopes.katana.ap.settings;

import java.util.Objects;
import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.checker.optional.qual.MaybePresent;

/**
 * Encapsulation of a value of an evaluated setting. This contains the parsed value as well as the
 * language element references to use within logging facilities to report compilation issues.
 *
 * @param <T> the value type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
final class SettingValueHolder<T> {

  private final T value;
  private final @PolyNull Element declaringElement;
  private final @PolyNull AnnotationMirror annotationMirror;
  private final @PolyNull AnnotationValue annotationValue;

  public SettingValueHolder(T value) {
    this.value = value;
    this.declaringElement = null;
    this.annotationMirror = null;
    this.annotationValue = null;
  }

  public SettingValueHolder(
      T value,
      Element declaringElement,
      AnnotationMirror annotationMirror,
      AnnotationValue annotationValue
  ) {
    this.value = Objects.requireNonNull(value);
    this.declaringElement = Objects.requireNonNull(declaringElement);
    this.annotationMirror = Objects.requireNonNull(annotationMirror);
    this.annotationValue = Objects.requireNonNull(annotationValue);
  }

  public T getValue() {
    return this.value;
  }

  @MaybePresent
  public Optional<Element> getDeclaringElement() {
    return Optional.ofNullable(this.declaringElement);
  }

  @MaybePresent
  public Optional<AnnotationMirror> getAnnotationMirror() {
    return Optional.ofNullable(this.annotationMirror);
  }

  @MaybePresent
  public Optional<AnnotationValue> getAnnotationValue() {
    return Optional.ofNullable(this.annotationValue);
  }
}
