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

package io.ascopes.katana.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation to instruct Katana to generate a mutable model from an annotated interface.
 *
 * <p>You <strong>must</strong> annotate an interface with this for Katana to bother analysing it at
 * compile time as a mutable model.
 *
 * <p>You may also annotate a package with this annotation and provide a set of Settings overrides,
 * if you wish to apply settings on a package-level only to mutable models.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface MutableModel {

  /**
   * A custom override for code-generation settings. You usually do not need to specify this.
   *
   * <p>Anything not overridden here will be inherited from any {@link Settings} annotation on the
   * interface itself, and then from any {@link Settings} annotation applied on a package level.
   *
   * <p>If nothing is overridden, then sensible defaults will be used.
   *
   * @return the model-specific settings to use.
   */
  Settings value() default @Settings;
}
