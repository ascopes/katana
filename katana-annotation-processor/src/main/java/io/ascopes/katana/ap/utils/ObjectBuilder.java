package io.ascopes.katana.ap.utils;

/**
 * Functional base for any builder types.
 *
 * @param <T> the result type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
public abstract class ObjectBuilder<T> {

  /**
   * Build the result.
   *
   * @return the built result.
   */
  public abstract T build();
}
