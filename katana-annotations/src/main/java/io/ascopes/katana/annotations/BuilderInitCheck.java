package io.ascopes.katana.annotations;

/**
 * Initialization checking mode for builders.
 *
 * <p>The purpose of this is to determine if attributes have been specified explicitly or not
 * when using a generated builder.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public enum BuilderInitCheck {
  /**
   * Do not check anything. Any uninitialized values are just ignored.
   *
   * <p>This is the most efficient option in terms of binary size and runtime overhead, but also
   * the most error-prone from a developer perspective.
   */
  NONE,

  /**
   * Check that attributes are initialized at runtime, reporting every attribute that is not
   * initialized in the message of the exception that would be thrown.
   *
   * <p>It is worth noting that this will still expect null/empty values to be initialized
   * explicitly.
   */
  RUNTIME,

  /**
   * Generate a "staged" type-safe builder. This will generate interfaces for every step that the
   * builder has that correspond to non-optional attributes.
   *
   * <p>This makes missing an attribute out into a compile-time error, at the cost of an additional
   * interface being defined per mandatory attribute.
   */
  TYPESAFE,
}
