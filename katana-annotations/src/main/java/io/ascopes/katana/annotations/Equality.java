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
 * Equality and identity generation policy. This generates an override for {@link
 * Object#equals(Object)} and {@link Object#hashCode()}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@CustomMethodAdvice(
    annotation = Equality.CustomEquals.class,
    returns = boolean.class,
    consumes = {This.class, Object.class}
)
@CustomMethodAdvice(
    annotation = Equality.CustomHashCode.class,
    returns = int.class,
    consumes = This.class
)
@ExclusionAdvice(annotation = Equality.Exclude.class)
@InclusionAdvice(annotation = Equality.Include.class)
@SuppressWarnings("unused")
public enum Equality implements CustomizableAttributeFeature {
  /**
   * Use the identity of the object for comparison. This prevents the methods being overridden in
   * the implementation, instead deferring to {@link Object#equals(Object)} and {@link
   * Object#hashCode()}.
   */
  DISABLE {
    @Override
    public boolean isDisabled() {
      return true;
    }
  },

  /**
   * Generate default {@link #equals(Object)} and {@link #hashCode()} methods that compare defined
   * attributes. All attributes will be considered in these methods unless explicitly marked with
   * {@link Exclude} (i.e. {@literal @Exclude(Equality.class)}).
   */
  INCLUDE_ALL {
    @Override
    public boolean isIncludeAll() {
      return true;
    }
  },

  /**
   * Generate default {@link #equals(Object)} and {@link #hashCode()} methods that compare defined
   * attributes. No attributes will be considered in these methods unless explicitly marked with
   * {@link Include} (i.e. {@literal @Include(Equality.class)}).
   */
  EXCLUDE_ALL {
    @Override
    public boolean isExcludeAll() {
      return true;
    }
  },

  /**
   * Use custom implementations for equality and identity. This requires two static methods to be
   * defined in your interface, one annotated as {@link CustomEquals} and one annotated as {@link
   * CustomHashCode}.
   *
   * <p>Not specifying these methods is an error.
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
   * Annotation to mark a static method as being the custom equals implementation.
   *
   * <p>The method must take the non-null interface it is defined in as the first argument,
   * and a nullable {@link Object} as the second argument. It must always return a {@code boolean}
   * primitive value.
   */
  @Documented
  @Retention(RetentionPolicy.SOURCE)
  @Target(ElementType.METHOD)
  public @interface CustomEquals {
    // Marker annotation only.
  }

  /**
   * Annotation to mark a static method as being the custom hashCOde implementation.
   *
   * <p>The method must take the non-null interface it is defined in as the sole argument,
   * and must always return an {@code int} primitive value.
   */
  @Documented
  @Retention(RetentionPolicy.SOURCE)
  @Target(ElementType.METHOD)
  public @interface CustomHashCode {
    // Marker annotation only.
  }
}
