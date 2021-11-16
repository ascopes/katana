package io.ascopes.katana.annotations;

import io.ascopes.katana.annotations.internal.ExclusionMarker;
import io.ascopes.katana.annotations.internal.InclusionMarker;
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
public enum Equality {
  /**
   * Inherit the setting.
   */
  INHERITED,

  /**
   * Disable generation of equality and identity methods explicitly.
   */
  DISABLED,

  /**
   * Generate default {@link #equals(Object)} and {@link #hashCode()} methods that compare defined
   * attributes. All attributes will be considered in these methods unless explicitly marked with
   * {@link Exclude} (i.e. {@literal @Exclude(Equality.class)}).
   */
  INCLUDE_ALL,

  /**
   * Generate default {@link #equals(Object)} and {@link #hashCode()} methods that compare defined
   * attributes. No attributes will be considered in these methods unless explicitly marked with
   * {@link Include} (i.e. {@literal @Include(Equality.class)}).
   */
  EXCLUDE_ALL,

  /**
   * Use custom implementations for equality and identity. This requires two static methods to be
   * defined in your interface:
   * <p>
   * <ol>
   *   <li><code>public static boolean isEqual(ThisInterface self, Object other)</code></li>
   *   <li><code>public static int hashCodeOf(ThisInterface self)</code></li>
   * </ol>
   * <p>
   * (where {@code ThisInterface} is the name of the interface you are using this annotation in).
   * <p>
   * Not specifying these methods is an error.
   */
  CUSTOM;

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
