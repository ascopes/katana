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

import io.ascopes.katana.annotations.Settings;
import java.util.Objects;
import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import org.checkerframework.checker.optional.qual.MaybePresent;


/**
 * Descriptor for an individual setting value. A collection of these are expected to be able to
 * represent a hierarchy of inheritance, where settings specified on a more specific level can
 * override those on a less specific level.
 *
 * <p>An example of this is that a {@link Settings} annotation on an interface should take
 * preference over one specified on a package level. Likewise the package level should take
 * preference over the default framework settings.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class SettingDescriptor<T> {

  private final SettingValueDescriptor<T> valueHolder;
  private final String description;
  private final SettingSchemaDescriptor<T> settingSchemaDescriptor;

  /**
   * Initialize the setting.
   *
   * @param valueHolder             the value holder for the setting value.
   * @param description             the description of the setting location.
   * @param settingSchemaDescriptor the schema for the setting.
   */
  public SettingDescriptor(
      SettingValueDescriptor<T> valueHolder,
      String description,
      SettingSchemaDescriptor<T> settingSchemaDescriptor
  ) {
    // Annotation values can't be null, so this is a perfectly valid assumption to be making.
    this.valueHolder = Objects.requireNonNull(valueHolder);
    this.description = Objects.requireNonNull(description);
    this.settingSchemaDescriptor = Objects.requireNonNull(settingSchemaDescriptor);
  }

  /**
   * Get the setting value.
   *
   * @return the value of the setting.
   */
  public T getValue() {
    return this.valueHolder.getValue();
  }

  /**
   * Get the setting description.
   *
   * @return the description of the setting.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Get the declaring element where this setting was explicitly specified.
   *
   * <p>If the setting was left as a Katana framework default value, then this will not be present.
   *
   * @return the declaring element in an optional, or an empty optional if this was a default value.
   */
  public Optional<Element> getDeclaringElement() {
    return this.valueHolder.getDeclaringElement();
  }

  /**
   * Get the annotation mirror for this setting, if known and if a declaring element is known.
   *
   * <p>If the setting was left as a Katana framework default value, then this will not be present.
   *
   * @return the annotation mirror in an optional, or an empty optional if not known.
   */
  @MaybePresent
  public Optional<AnnotationMirror> getAnnotationMirror() {
    return this.valueHolder.getAnnotationMirror();
  }

  /**
   * Get the annotation value for this setting, if known and if a declaring element and mirror is
   * known.
   *
   * <p>If the setting was left as a Katana framework default value, then this will not be present.
   *
   * @return the annotation value in an optional, or an empty optional if not known.
   */
  @MaybePresent
  public Optional<AnnotationValue> getAnnotationValue() {
    return this.valueHolder.getAnnotationValue();
  }

  /**
   * Get the generic type for the setting.
   *
   * @return the generic type for the setting.
   */
  public Class<T> getType() {
    return this.settingSchemaDescriptor.getParameterizedTypeish();
  }

  /**
   * Get the setting schema.
   *
   * @return the setting schema.
   */
  SettingSchemaDescriptor<T> getSettingSchema() {
    return this.settingSchemaDescriptor;
  }
}
