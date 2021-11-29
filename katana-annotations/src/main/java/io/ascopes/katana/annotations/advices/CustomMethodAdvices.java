package io.ascopes.katana.annotations.advices;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Repeatable wrapper for {@link CustomMethodAdvice}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CustomMethodAdvices {

  /**
   * Get the associated advices.
   *
   * @return the advice annotations.
   */
  CustomMethodAdvice[] value();
}
