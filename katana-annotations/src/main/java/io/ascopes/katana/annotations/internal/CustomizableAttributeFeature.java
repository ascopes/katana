package io.ascopes.katana.annotations.internal;

/**
 * Extension of {@link AttributeFeature} which also supports providing custom implementations by the
 * end user.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public interface CustomizableAttributeFeature extends AttributeFeature {

  /**
   * Check if the attribute feature requests a custom implementation to be used.
   *
   * @return true if the value represents a custom implementation should be used.
   */
  default boolean isCustom() {
    return false;
  }
}
