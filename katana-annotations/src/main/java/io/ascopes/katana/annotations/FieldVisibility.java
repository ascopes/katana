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
 * Annotation to specify the visibility of the underlying field for an attribute.
 *
 * <p>It is usually not recommended changing this unless you have a good reason to, as it
 * defeats the principle of using behaviours as a means of accessing and modifying information.
 *
 * <p>Fields are considered an implementation detail first and foremost within Katana, so this
 * may also threaten stability of your builds if reliance on features such as volatility and
 * final-ness are relied on.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface FieldVisibility {

  /**
   * The visibility to use for this field.
   *
   * @return the visibility.
   */
  Visibility value();
}
