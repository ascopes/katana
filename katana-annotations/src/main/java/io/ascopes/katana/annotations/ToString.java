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

import io.ascopes.katana.annotations.internal.CustomMethodAdvice;
import io.ascopes.katana.annotations.internal.CustomMethodAdvice.This;
import io.ascopes.katana.annotations.internal.CustomizableAttributeFeature;
import io.ascopes.katana.annotations.internal.ExclusionAdvice;
import io.ascopes.katana.annotations.internal.InclusionAdvice;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Policy to use for generation of a {@link #toString()} override.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@CustomMethodAdvice(
    annotation = ToString.CustomToString.class,
    returns = String.class,
    consumes = This.class
)
@ExclusionAdvice(annotation = ToString.Exclude.class)
@InclusionAdvice(annotation = ToString.Include.class)
@SuppressWarnings("unused")
public enum ToString implements CustomizableAttributeFeature {
  /**
   * Do not generate a toString method. The {@link Object#toString()} method will be used instead.
   */
  DISABLE {
    @Override
    public boolean isDisabled() {
      return true;
    }
  },

  /**
   * Include all attributes from the generated {@link Object#toString()} unless explicitly
   * excluded.
   */
  INCLUDE_ALL {
    @Override
    public boolean isIncludeAll() {
      return true;
    }
  },

  /**
   * Exclude all attributes from the generated {@link Object#toString()} unless explicitly
   * included.
   */
  EXCLUDE_ALL {
    @Override
    public boolean isExcludeAll() {
      return true;
    }
  },

  /**
   * Use custom implementations for the {@link Object#toString()}. This requires a static method to
   * be defined and be annotated with {@link CustomToString}.
   *
   * <p>Not specifying this method is an error.
   */
  CUSTOM {
    @Override
    public boolean isCustom() {
      return true;
    }
  };

  /**
   * Annotation to explicitly enable checking for an attribute.
   */
  @Documented
  @Retention(RetentionPolicy.SOURCE)
  @Target(ElementType.METHOD)
  public @interface Include {
    // Marker annotation only.
  }

  /**
   * Annotation to explicitly disable checking for an attribute.
   */
  @Documented
  @Retention(RetentionPolicy.SOURCE)
  @Target(ElementType.METHOD)
  public @interface Exclude {
    // Marker annotation only.
  }

  /**
   * Annotation to mark a static method as being the custom toString implementation.
   *
   * <p>The method must take the interface it is defined in as the sole argument, and return a
   * non-null {@link String}.
   */
  @Documented
  @Retention(RetentionPolicy.SOURCE)
  @Target(ElementType.METHOD)
  public @interface CustomToString {
    // Marker annotation only.
  }
}
