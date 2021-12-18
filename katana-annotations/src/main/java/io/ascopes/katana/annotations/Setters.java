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

import io.ascopes.katana.annotations.internal.AttributeFeature;
import io.ascopes.katana.annotations.internal.ExclusionAdvice;
import io.ascopes.katana.annotations.internal.InclusionAdvice;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Policy for generating setter methods on mutable models.
 *
 * <p>This is ignored for immutable models (mutability on an immutable object is meaningless).
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@ExclusionAdvice(annotation = Setters.Exclude.class)
@InclusionAdvice(annotation = Setters.Include.class)
@SuppressWarnings("unused")
public enum Setters implements AttributeFeature {
  /**
   * Disable generation of setters entirely.
   */
  DISABLED {
    @Override
    public boolean isDisabled() {
      return true;
    }
  },

  /**
   * Generate setter methods for all attributes that are not excluded with {@link Exclude} (i.e.
   * {@literal @Exclude(Setters.class)}).
   */
  INCLUDE_ALL {
    @Override
    public boolean isIncludeAll() {
      return true;
    }
  },

  /**
   * Generate setter methods for no attributes except those included with {@link Include} (i.e.
   * {@literal @Include(Setters.class)}).
   */
  EXCLUDE_ALL {
    @Override
    public boolean isExcludeAll() {
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
}
