package io.ascopes.katana.annotations;

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
@ExclusionAdvice(Equality.Exclude.class)
@InclusionAdvice(Equality.Include.class)
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
   * defined in your interface:
   *
   * <ol>
   *   <li><code>public static boolean isEqual(ThisInterface self, Object other)</code></li>
   *   <li><code>public static int hashCodeOf(ThisInterface self)</code></li>
   * </ol>
   *
   * <p>...where {@code ThisInterface} is the name of the interface you are using this annotation
   * in.
   *
   * <p>Not specifying these methods is an error.
   */
  CUSTOM {
    @Override
    public boolean isCustomImpl() {
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

  }

  /**
   * Annotation to explicitly disable checking for an attribute.
   */
  @Documented
  @Retention(RetentionPolicy.SOURCE)
  @Target(ElementType.METHOD)
  public @interface Exclude {

  }
}
