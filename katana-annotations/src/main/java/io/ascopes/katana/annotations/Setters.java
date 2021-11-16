package io.ascopes.katana.annotations;

import io.ascopes.katana.annotations.internal.ExclusionMarker;
import io.ascopes.katana.annotations.internal.InclusionMarker;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Policy for generating setter methods on mutable models, and "wither" methods on immutable
 * models.
 * <p>
 * Wither methods are setters that return a new instance of the object they apply to, rather than
 * mutating the current object. In this respect, they are threadsafe.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public enum Setters {
  /**
   * Inherit the setting.
   */
  INHERITED,

  /**
   * Disable generation of setters entirely.
   */
  DISABLED,

  /**
   * Generate setter methods for all attributes that are not excluded with {@link Exclude} (i.e.
   * {@literal @Exclude(Setters.class)}).
   */
  INCLUDE_ALL,

  /**
   * Generate setter methods for no attributes except those included with {@link Include} (i.e.
   * {@literal @Include(Setters.class)}).
   */
  EXCLUDE_ALL;

  /**
   * Annotation to explicitly enable checking for an attribute.
   */
  @Documented
  @InclusionMarker
  @Retention(RetentionPolicy.SOURCE)
  @Target(ElementType.METHOD)
  public @interface Include {

  }

  /**
   * Annotation to explicitly disable checking for an attribute.
   */
  @Documented
  @ExclusionMarker
  @Retention(RetentionPolicy.SOURCE)
  @Target(ElementType.METHOD)
  public @interface Exclude {

  }
}
