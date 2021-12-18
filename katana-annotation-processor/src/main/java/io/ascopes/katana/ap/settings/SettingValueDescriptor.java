/*
 * Copyright (C) 2021 Ashley Scopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
final class SettingValueDescriptor<T> {

  private final T value;
  private final @PolyNull Element declaringElement;
  private final @PolyNull AnnotationMirror annotationMirror;
  private final @PolyNull AnnotationValue annotationValue;

  /**
   * Initialize the setting holder from a Katana default value.
   */
  public SettingValueDescriptor(T value) {
    this.value = value;
    this.declaringElement = null;
    this.annotationMirror = null;
    this.annotationValue = null;
  }

  /**
   * Initialize the setting holder from an explicitly provided value.
   *
   * @param value            the value.
   * @param declaringElement the declaring element.
   * @param annotationMirror the annotation mirror.
   * @param annotationValue  the annotation value.
   */
  public SettingValueDescriptor(
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
   * Get the setting value.
   *
   * @return the setting value.
   */
  public T getValue() {
    return this.value;
  }

  /**
   * Get the declaring element where this setting was explicitly specified.
   *
   * <p>If the setting was left as a Katana framework default value, then this will not be present.
   *
   * @return the declaring element in an optional, or an empty optional if this was a default value.
   */
  @PolyPresent
  public Optional<Element> getDeclaringElement() {
    return Optional.ofNullable(this.declaringElement);
  }

  /**
   * Get the annotation mirror for this setting, if known and if a declaring element is known.
   *
   * <p>If the setting was left as a Katana framework default value, then this will not be present.
   *
   * @return the annotation mirror in an optional, or an empty optional if not known.
   */
  @PolyPresent
  public Optional<AnnotationMirror> getAnnotationMirror() {
    if (this.declaringElement == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(this.annotationMirror);
  }

  /**
   * Get the annotation value for this setting, if known and if a declaring element and mirror is
   * known.
   *
   * <p>If the setting was left as a Katana framework default value, then this will not be present.
   *
   * @return the annotation value in an optional, or an empty optional if not known.
   */
  @PolyPresent
  public Optional<AnnotationValue> getAnnotationValue() {
    if (this.annotationMirror == null || this.declaringElement == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(this.annotationValue);
  }
}
