package io.ascopes.katana.annotations.internal;

import io.ascopes.katana.annotations.Settings;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for each annotation attribute within {@link Settings} to enable the specification of
 * global defaults for settings that apply to immutable types only.
 * <p>
 * This annotation does nothing outside the {@link Settings} class, and should not be used by
 * users.
 *
 * @author Ashley Scopes
 * @see DefaultSetting
 * @see MutableDefaultSetting
 * @since 0.0.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ImmutableDefaultSetting {

  /**
   * The raw values to parse as the global default settings for immutable types, should nothing
   * user-defined override the annotated setting anywhere. For arrays, more than one value can be
   * passed. For non-array types, it is an error to provide anything other than one argument here.
   *
   * @return the raw value to use.
   */
  String[] value();
}
