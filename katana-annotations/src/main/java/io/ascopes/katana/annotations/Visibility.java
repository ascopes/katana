package io.ascopes.katana.annotations;

/**
 * Visibility of a member.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public enum Visibility {
  /**
   * Corresponds to the {@link java.lang.reflect.Modifier#PUBLIC} modifier.
   */
  PUBLIC,

  /**
   * Corresponds to the {@link java.lang.reflect.Modifier#PROTECTED} modifier.
   */
  PROTECTED,

  /**
   * Corresponds to the implicit {@code PACKAGE_PRIVATE} modifier.
   */
  PACKAGE_PRIVATE,

  /**
   * Corresponds to the {@link java.lang.reflect.Modifier#PRIVATE} modifier.
   */
  PRIVATE,
}
