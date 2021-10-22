package io.ascopes.katana.annotations;

/**
 * Marker interface for attribute-related policies that can be used with the
 * {@link Include} and {@link Exclude} annotations on interface methods.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public interface AttributePolicy {
  /**
   * Return true if the feature policy is inherited from somewhere else.
   */
  default boolean isInherited() {
    return false;
  }

  /**
   * Return true if the feature is disabled.
   */
  default boolean isDisabled() {
    return false;
  }

  /**
   * Return true if the feature policy includes all attributes by default.
   */
  default boolean isIncludeAll() {
    return false;
  }

  /**
   * Return true if the feature policy excludes all attributes by default.
   */
  default boolean isExcludeAll() {
    return false;
  }

  /**
   * Return true if the policy was explicitly specified, and thus is not inherited from anywhere
   * else.
   */
  default boolean isExplicitlySpecified() {
    return !isInherited();
  }
}
