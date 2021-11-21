package io.ascopes.katana.annotations;

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
public enum ToString {
  /**
   * Do not generate a toString method.
   */
  DISABLED,

  /**
   * Include all attributes from the generated {@link #toString()} unless explicitly excluded.
   */
  INCLUDE_ALL,

  /**
   * Exclude all attributes from the generated {@link #toString()} unless explicitly included.
   */
  EXCLUDE_ALL,

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
