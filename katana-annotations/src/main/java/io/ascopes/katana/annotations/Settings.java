/*
 * Copyright (C) 2021 Ashley Scopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 *
 * <p>There are six ways to specify settings for your models:
 *
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
 *
 * <p>This annotation takes precedence in the order specified above, from #1 with the
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
   * The pattern to use for package names. An asterisk "{@code *}" can be used to substitute the
   * interface package (e.g. {@code "*.impl"}).
   *
   * @return the pattern string.
   */
  String packageName() default "*.impl";

  /**
   * The pattern to use for class names. An asterisk "{@code *}" can be used to substitute the
   * interface name (e.g. {@code "*Impl"} or {@code "Immutable*Impl"}).
   *
   * @return the pattern string.
   */
  @ImmutableDefaultAdvice("Immutable*")
  @MutableDefaultAdvice("Mutable*")
  String className() default "";

  ////////////////////////
  //// Field settings ////
  ////////////////////////

  /**
   * Whether to make all internal fields transient by default, or whether to make them non-transient
   * by default.
   *
   * @return the transience preference.
   */
  Transient fieldTransience() default Transient.EXCLUDE_ALL;

  /**
   * The default visibility to give fields. This can be overridden per field using the {@link
   * FieldVisibility annotation} on a getter.
   *
   * @return the field visibility.
   */
  Visibility fieldVisibility() default Visibility.PRIVATE;

  //////////////////////////////////////////////////
  //// Construction and initialization settings ////
  //////////////////////////////////////////////////

  /**
   * True if an all-arguments constructor should be added.
   *
   * @return true or false.
   */
  boolean allArgsConstructor() default false;

  /**
   * True if a default constructor should be added. For mutable types this is a no-args constructor,
   * and for immutable types this is the same as {@link #allArgsConstructor()}.
   *
   * @return true or false.
   */
  boolean defaultArgsConstructor() default true;

  /**
   * True if a copy-constructor should be added.
   *
   * @return true or false.
   */
  boolean copyConstructor() default false;

  /////////////////////////////////////
  //// Builder generation settings ////
  /////////////////////////////////////

  /**
   * The builder implementation to use.
   *
   * @return the builder implementation to use.
   */
  Builder builder() default Builder.DISABLED;

  /**
   * The name to give the builder, if enabled. Ignored if {@link #builder()} is false.
   *
   * @return the name.
   */
  String builderClassName() default "Builder";

  /**
   * Name of the builder "build" method. This is ignored if {@link #builder()} is false.
   *
   * @return the name of the method that builds a model instance from a builder.
   */
  String builderBuildMethodName() default "build";

  /**
   * Name of the builder initialization method that is defined on the generated model type. This is
   * ignored if {@link #builder()} is false.
   *
   * @return the builder initialization method name.
   */
  String initBuilderMethodName() default "builder";

  /**
   * True if a {@code toBuilder} method should be added to models that have a supported builder.
   * This is ignored if {@link #builder()} is false.
   *
   * @return true or false.
   */
  boolean toBuilderMethodEnabled() default false;

  /**
   * Name of the toBuilder method. This is ignored if {@link #toBuilderMethodEnabled()} or {@link
   * #builder()} are false.
   *
   * @return the toBuilder method name.
   */
  String toBuilderMethodName() default "toBuilder";

  ///////////////////////////
  //// Accessor settings ////
  ///////////////////////////

  /**
   * The list of fully qualified class names to treat as boolean types. This will always include the
   * primitive type implicitly, and this should not be added explicitly to this array.
   *
   * @return an array of unique class names.
   */
  Class<?>[] booleanTypes() default {Boolean.class, AtomicBoolean.class};

  /**
   * The name for boolean getter methods. If this is empty, the getter will be treated as a fluent
   * accessor (no prefix, camelcase).
   *
   * @return the prefix string.
   */
  String booleanGetterPrefix() default "is";

  /**
   * The name for regular getter methods. If this is empty, the getter will be treated as a fluent
   * accessor (no prefix, camelcase).
   *
   * @return the prefix string.
   */
  String getterPrefix() default "get";

  //////////////////////////
  //// Mutator settings ////
  //////////////////////////

  /**
   * The policy for generating setter methods. On immutable types, this will be ignored.
   *
   * @return the Setters policy.
   */
  Setters setters() default Setters.INCLUDE_ALL;

  /**
   * The prefix for setter methods. If this is empty, the setter will be implemented as a fluent
   * mutator (no prefix, camelcase).
   *
   * @return the prefix string.
   */
  String setterPrefix() default "set";

  /////////////////////////////////////////////////
  //// equals and hashCode generation settings ////
  /////////////////////////////////////////////////

  /**
   * The policy for generating {@link Object#equals} and {@link Object#hashCode()} overrides.
   *
   * @return the equality policy.
   */
  Equality equalityMode() default Equality.INCLUDE_ALL;

  //////////////////////////////////////
  //// toString generation settings ////
  //////////////////////////////////////

  /**
   * The policy for generating a {@link Object#toString()} override.
   *
   * @return the ToString policy.
   */
  ToString toStringMode() default ToString.INCLUDE_ALL;

  ////////////////////
  //// Aesthetics ////
  ////////////////////

  /**
   * The indent to use in generated code. You probably won't care about this. It defaults to a
   * 4-space indent.
   *
   * <p>For tabs, set the string to {@code "\t"} instead.
   *
   * @return the indent string to use.
   */
  String indent() default "    ";

  ///////////////////////
  //// Null-checking ////
  ///////////////////////

  /**
   * Determine whether to enforce non-null checks.
   *
   * <p>If enabled, null values are not allowed to be provided for attributes unless the type
   * is wrapped in {@link java.util.Optional}, or is annotated with {@link NullableAttribute}.
   *
   * <p>It is also worth noting that this will disallow overriding a non-null attribute in a
   * super-interface with one that is nullable. You can however override a nullable attribute in a
   * super-interface with one that is non-null by using the {@link NullableAttribute} annotation
   * with a value of {@code false}.
   *
   * @return true if null-checking is enabled, or false if it is disabled.
   */
  boolean enforceNonNull() default true;
}
