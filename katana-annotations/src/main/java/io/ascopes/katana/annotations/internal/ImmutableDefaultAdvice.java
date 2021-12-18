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

package io.ascopes.katana.annotations.internal;

import io.ascopes.katana.annotations.Settings;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for each annotation attribute within {@link Settings} to enable the specification of
 * global defaults for settings that apply to immutable types only.
 *
 * <p>This annotation does nothing outside the {@link Settings} class, and should not be used by
 * users.
 *
 * @author Ashley Scopes
 * @see MutableDefaultAdvice
 * @since 0.0.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ImmutableDefaultAdvice {

  /**
   * The raw values to parse as the global default settings for immutable types, should nothing
   * user-defined override the annotated setting anywhere. For arrays, more than one value can be
   * passed. For non-array types, it is an error to provide anything other than one argument here.
   *
   * @return the raw value to use.
   */
  String[] value();
}
