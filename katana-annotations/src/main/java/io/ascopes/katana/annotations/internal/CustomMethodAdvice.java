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

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation for custom method implementations that describes the signature of the things that
 * the annotated advice can decorate.
 *
 * <p>Used for marking features like custom toString providers in a type-safe way.
 *
 * <p>This should be applied to the feature type itself.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@Documented
@Repeatable(CustomMethodAdvices.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CustomMethodAdvice {

  /**
   * Get the annotation to use for the advice.
   *
   * @return the annotation to use for the advice.
   */
  Class<? extends Annotation> annotation();

  /**
   * Get the required return type from the method advice.
   *
   * @return the required return type.
   */
  Class<?> returns();

  /**
   * Get the required argument types from the method advice.
   *
   * @return the required argument types.
   */
  Class<?>[] consumes();

  /**
   * A marker class used to represent the type of the interface it is used within.
   */
  final class This {
    private This() {
      throw new UnsupportedOperationException();
    }
  }
}
