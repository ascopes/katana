package io.ascopes.katana.ap.descriptors;

/**
 * Types of constructor to support.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public enum Constructor {
  /**
   * A constructor consuming all fields.
   */
  ALL_ARGS,

  /**
   * A constructor consuming no fields. This makes little sense for immutable types.
   */
  NO_ARGS,

  /**
   * A copy-constructor.
   */
  COPY,
}
