package io.ascopes.katana.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation to instruct Katana to generate an immutable model from an annotated interface.
 * <p>
 * It is worth noting that it is an error to annotate a non-interface with this annotation.
 * <p>
 * You <strong>must</strong> annotate an interface with this for Katana to bother analysing it
 * at compile time as an immutable model.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ImmutableModel {
  /**
   * Return custom overrides for code-generation settings.
   * <p>
   * Anything not overridden here will be inherited from any {@link Settings} annotation on the
   * interface itself, and then from any {@link Settings} annotation applied on a package level.
   * <p>
   * If nothing is overridden, then sensible defaults will be used.
   */
  Settings settings() default @Settings;
}
