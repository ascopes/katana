package io.ascopes.katana.annotations;

import io.ascopes.katana.annotations.internal.ImmutableDefaultAdvice;
import io.ascopes.katana.annotations.internal.MutableDefaultAdvice;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Annotation to override defaults for code generation.
 * <p>
 * There are six ways to specify settings for your models:
 * <p>
 * <ol>
 *   <li>
 *     Apply it to a specific generated model, by specifying it in {@link MutableModel#value()}
 *     or {@link ImmutableModel#value()}
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
 *     Apply it to all models generated from interfaces in a package for immutable or mutable
 *     models specifically. You can do this by creating a {@code package-info.java} and
 *     annotating the {@code package} declaration with either {@link MutableModel} or
 *     {@link ImmutableModel} and pass the desired settings as the argument.
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
@SuppressWarnings("unused")
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface Settings {
  //////////////////////////////
  //// Type-naming settings ////
  //////////////////////////////

  /**
   * Return the pattern to use for package names. An asterisk "{@code *}" can be used to substitute
   * the interface package (e.g. {@code "*.impl"}).
   */
  String packageName() default "*.impl";

  /**
   * Return the pattern to use for class names. An asterisk "{@code *}" can be used to substitute
   * the interface name (e.g. {@code "*Impl"} or {@code "Immutable*Impl"}).
   */
  @ImmutableDefaultAdvice("Immutable*")
  @MutableDefaultAdvice("Mutable*")
  String className() default "";

  //////////////////////////////////////////////////
  //// Construction and initialization settings ////
  //////////////////////////////////////////////////

  /**
   * Return whether to generate an all-arguments constructor for models.
   */
  boolean allArgsConstructor() default false;

  /**
   * Return whether to generate a copy constructor for models.
   */
  boolean copyConstructor() default true;

  /**
   * Return whether to generate a default constructor for models. This is the same as all-arguments
   * for immutable types, and is a constructor for all non-null attributes on mutable types that
   * lack default values. If you do not have non-null attributes without default values on mutable
   * types, then this will generate a no-arguments constructor.
   */
  boolean defaultConstructor() default true;

  /**
   * Return whether to generate an all-arguments constructor for immutable models.
   */
  boolean builder() default false;

  ///////////////////////////
  //// Accessor settings ////
  ///////////////////////////

  /**
   * Return the list of fully qualified class names to treat as boolean types. This will always
   * include the primitive type implicitly.
   */
  Class<?>[] booleanTypes() default {Boolean.class, AtomicBoolean.class};

  /**
   * Return the name for boolean getter methods.
   */
  String booleanGetterPrefix() default "is";

  /**
   * Return the name for regular getter methods.
   */
  String getterPrefix() default "get";

  //////////////////////////
  //// Mutator settings ////
  //////////////////////////


  /**
   * Return the policy for generating setter methods. On immutable types, this will be implemented
   * as a "wither" method.
   */
  Setters setters() default Setters.INCLUDE_ALL;

  /**
   * Return the name for setter methods.
   */
  @ImmutableDefaultAdvice("with")
  @MutableDefaultAdvice("set")
  String setterPrefix() default "";

  /////////////////////////////////////////////////
  //// equals and hashCode generation settings ////
  /////////////////////////////////////////////////

  /**
   * Return the policy for generating {@link Object#equals} and {@link Object#hashCode()}
   * overrides.
   */
  Equality equalsAndHashCode() default Equality.INCLUDE_ALL;

  //////////////////////////////////////
  //// toString generation settings ////
  //////////////////////////////////////

  /**
   * Return the policy for generating a {@link Object#toString()} override.
   */
  ToString toStringMethod() default ToString.INCLUDE_ALL;
}
