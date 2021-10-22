package io.ascopes.katana.annotations;

import io.ascopes.katana.annotations.internal.ExclusionMarker;
import io.ascopes.katana.annotations.internal.InclusionMarker;
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
public enum ToStringSetting implements AttributePolicy {
  /**
   * Inherit the settings to use.
   */
  INHERIT {
    /** {@inheritDoc} */
    @Override
    public boolean isInherited() {
      return true;
    }
  },

  /**
   * Do not generate a toString method.
   */
  DISABLE {
    /** {@inheritDoc} */
    @Override
    public boolean isDisabled() {
      return true;
    }
  },

  /**
   * Include all attributes from the generated {@link #toString()} unless explicitly excluded.
   */
  INCLUDE_ALL {
    /** {@inheritDoc} */
    @Override
    public boolean isIncludeAll() {
      return true;
    }
  },

  /**
   * Exclude all attributes from the generated {@link #toString()} unless explicitly included.
   */
  EXCLUDE_ALL {
    /** {@inheritDoc} */
    @Override
    public boolean isExcludeAll() {
      return true;
    }
  },

  /**
   * Use custom implementations for the {@link #toString()}. This requires a static method to be
   * defined with the following signature:
   * <p>
   * <code>public static String asString(ThisInterface self)</code>
   * <p>
   * ...where {@code ThisInterface} is the name of the interface you are using this annotation in.
   * <p>
   * Not specifying this method is an error.
   */
  CUSTOM;

  /**
   * Annotation to explicitly enable checking for an attribute.
   */
  @Documented
  @InclusionMarker
  @Retention(RetentionPolicy.SOURCE)
  @Target(ElementType.METHOD)
  @interface Include {
  }

  /**
   * Annotation to explicitly disable checking for an attribute.
   */
  @Documented
  @ExclusionMarker
  @Retention(RetentionPolicy.SOURCE)
  @Target(ElementType.METHOD)
  @interface Exclude {
  }
}
