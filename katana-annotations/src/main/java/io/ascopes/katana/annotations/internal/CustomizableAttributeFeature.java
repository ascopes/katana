package io.ascopes.katana.annotations.internal;

/**
 * Extension of {@link AttributeFeature} which also supports providing custom implementations
 * by the end user.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public interface CustomizableAttributeFeature extends AttributeFeature {
  /**
   * @return true if the value represents "custom implementation".
   */
  default boolean isCustomImpl() {
    return false;
  }
}
