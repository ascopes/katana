package io.ascopes.katana.annotations.internal;

/**
 * Interface for a descriptor describing a feature on an attribute.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public interface AttributeFeature extends Feature {

  /**
   * Determine whether the attribute feature would include all attributes implicitly
   * by default.
   *
   * @return true if the value represents "include all".
   */
  default boolean isIncludeAll() {
    return false;
  }

  /**
   * Determine whether the attribute feature would exclude all attributes implicitly
   * by default.
   *
   * @return true if the value represents "exclude all".
   */
  default boolean isExcludeAll() {
    return false;
  }
}
