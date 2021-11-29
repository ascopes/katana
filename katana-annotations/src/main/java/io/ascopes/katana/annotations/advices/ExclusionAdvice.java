package io.ascopes.katana.annotations.advices;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Internal annotation to advise that a feature can be excluded per attribute by a given annotation
 * value.
 *
 * <p>This should be applied to the feature enum itself.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExclusionAdvice {

  /**
   * Get the annotation to use to exclude the feature on an attribute.
   *
   * @return the annotation class.
   */
  Class<? extends Annotation> annotation();
}
