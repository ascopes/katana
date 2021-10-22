package io.ascopes.katana.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to override defaults for code generation.
 * <p>
 * There are five ways to specify settings for your models:
 * <p>
 * <ol>
 *   <li>
 *     Apply it to a specific generated model, by specifying it in {@link MutableModel#settings()}.
 *   </li>
 *   <li>
 *     Apply it to all models generated from a specific interface by annotating
 *     the interface with it.
 *   </li>
 *   <li>
 *     Inheriting it from a superinterface.
 *   </li>
 *   <li>
 *     Apply it to all models generated from interfaces in a package. You
 *     can do this by creating a {@code package-info.java} and annotating the
 *     {@code package} declaration with this annotation.
 *   </li>
 *   <li>
 *     Do not specify the annotation at all. In this case, sane defaults for Katana will be
 *     used. This is the default if you do not specify this annotation at all anywhere.
 *   </li>
 * </ol>
 * <p>
 * This annotation takes precedence in the order specified above, from #1 with the
 * greatest preference to #5 with the least preference.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface Settings {
  /**
   * Return the pattern to use for package names. An asterisk "{@code *}" can be used to substitute
   * the interface package (e.g. {@code "*.impl"}).
   */
  String packageName() default "";

  /**
   * Return the set of initializers to implement. If this is empty then the value is inherited or
   * a sane default is provided instead.
   */
  Initializer[] initializers() default {};

  /**
   * Return the policy for generating {@link Object#equals} and {@link Object#hashCode()} overrides.
   */
  EqualitySetting equality() default EqualitySetting.INHERIT;

  /**
   * Return the policy for generating a {@link Object#toString()} override.
   */
  ToStringSetting stringification() default ToStringSetting.INHERIT;

  /**
   * @return the policy for generating setter/wither methods.
   */
  SetterSetting setters() default SetterSetting.INHERIT;
}
