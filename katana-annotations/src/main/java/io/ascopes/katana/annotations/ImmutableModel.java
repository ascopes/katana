package io.ascopes.katana.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation to instruct Katana to generate an immutable model from an annotated interface.
 * <p>
 * You <strong>must</strong> annotate an interface with this for Katana to bother analysing it at
 * compile time as an immutable model.
 * <p>
 * You may also annotate a package with this annotation and provide a set of Settings overrides, if
 * you wish to apply settings on a package-level only to mutable models.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface ImmutableModel {

  /**
   * @return custom overrides for code-generation settings.
   * <p>
   * Anything not overridden here will be inherited from any {@link Settings} annotation on the
   * interface itself, and then from any {@link Settings} annotation applied on a package level.
   * <p>
   * If nothing is overridden, then sensible defaults will be used.
   */
  Settings value() default @Settings;
}
