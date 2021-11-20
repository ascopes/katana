package io.ascopes.katana.ap.settings;

import java.util.Objects;
import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.checker.optional.qual.PolyPresent;

/**
 * Encapsulation of a value of an evaluated setting. This contains the parsed value as well as the
 * language element references to use within logging facilities to report compilation issues.
 *
 * @param <T> the value type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
class SettingValueHolder<T> {

  private final T value;
  private final @PolyNull Element declaringElement;
  private final @PolyNull AnnotationMirror annotationMirror;
  private final @PolyNull AnnotationValue annotationValue;

  /**
   * Initialize a default setting holder.
   *
   * @param value the parsed default value.
   */
  public SettingValueHolder(T value) {
    this.value = value;
    this.declaringElement = null;
    this.annotationMirror = null;
    this.annotationValue = null;
  }

  /**
   * Initialize a user-specified setting holder.
   *
   * @param value            the parsed value.
   * @param declaringElement the element declaring the setting.
   * @param annotationMirror the mirror defining the setting.
   * @param annotationValue  the value of the setting within the annotation.
   */
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

  /**
   * @return the parsed value.
   */
  public T getValue() {
    return this.value;
  }

  /**
   * @return the declaring element defining the setting, or an empty optional if this is a default
   * setting.
   */
  public @PolyPresent Optional<Element> getDeclaringElement() {
    return Optional.ofNullable(this.declaringElement);
  }

  /**
   * @return the mirror defining the setting, or an empty optional if this is a default setting.
   */
  public @PolyPresent Optional<AnnotationMirror> getAnnotationMirror() {
    return Optional.ofNullable(this.annotationMirror);
  }

  /**
   * @return the value of the setting within the annotation, or an empty optional if this is a
   * default setting.
   */
  public @PolyPresent Optional<AnnotationValue> getAnnotationValue() {
    return Optional.ofNullable(this.annotationValue);
  }
}
