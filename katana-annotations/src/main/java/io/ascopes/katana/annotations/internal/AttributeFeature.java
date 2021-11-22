package io.ascopes.katana.annotations.internal;

/**
 * Interface for a descriptor describing a feature on an attribute.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public interface AttributeFeature {

  /**
   * @return true if the value represents "include all".
   */
  default boolean isIncludeAll() {
    return false;
  }

  /**
   * @return true if the value represents "exclude all".
   */
  default boolean isExcludeAll() {
    return false;
  }

  /**
   * @return true if the value represents "disabled".
   */
  default boolean isDisabled() {
    return false;
  }
}
