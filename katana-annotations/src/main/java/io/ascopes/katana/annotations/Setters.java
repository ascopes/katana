package io.ascopes.katana.annotations;

import io.ascopes.katana.annotations.features.AttributeFeature;
import io.ascopes.katana.annotations.advices.ExclusionAdvice;
import io.ascopes.katana.annotations.advices.InclusionAdvice;
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
