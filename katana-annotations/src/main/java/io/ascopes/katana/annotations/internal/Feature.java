package io.ascopes.katana.annotations.internal;

/**
 * Interface for any feature which can be enabled or disabled.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public interface Feature {
  /**
   * Determine whether the attribute feature would be explicitly skipped.
   *
   * @return true if the value represents "disabled".
   */
  default boolean isDisabled() {
    return false;
  }
}
