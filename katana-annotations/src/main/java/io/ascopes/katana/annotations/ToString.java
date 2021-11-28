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
 * Policy to use for generation of a {@link #toString()} override.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@ExclusionAdvice(ToString.Exclude.class)
@InclusionAdvice(ToString.Include.class)
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
   * be defined with the following signature:
   *
   * <p><code>public static String asString(ThisInterface self)</code>
   *
   * <p>...where {@code ThisInterface} is the name of the interface you are using this annotation
   * in.
   *
   * <p>Not specifying this method is an error if you use this setting.
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
