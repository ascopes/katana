package io.ascopes.katana.ap.methods;

import io.ascopes.katana.ap.utils.StringUtils;
import javax.lang.model.element.ExecutableElement;

/**
 * Strategy for producing a ToString method.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public abstract class ToStringStrategyDescriptor {

  private ToStringStrategyDescriptor() {
    // sealed class.
  }

  /**
   * A strategy for generating a default implementation where attributes may be explicitly included
   * or excluded.
   */
  public static final class GeneratedToStringStrategyDescriptor extends ToStringStrategyDescriptor {

    private final boolean includeAll;

    public GeneratedToStringStrategyDescriptor(boolean includeAll) {
      this.includeAll = includeAll;
    }

    /**
     * Return true if the strategy includes all attributes by default, or false if it excludes all
     * attributes by default.
     *
     * @return true or false.
     */
    public boolean isIncludeAll() {
      return this.includeAll;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return "GeneratedToStringStrategyDescriptor{"
          + "includeAll=" + this.includeAll
          + '}';
    }
  }

  /**
   * A strategy for using a custom defined toString method.
   */
  public static final class CustomToStringStrategyDescriptor extends ToStringStrategyDescriptor {

    private final ExecutableElement toStringMethod;

    /**
     * Initialize this strategy.
     *
     * @param toStringMethod the toString method to use.
     */
    public CustomToStringStrategyDescriptor(ExecutableElement toStringMethod) {
      this.toStringMethod = toStringMethod;
    }

    /**
     * Get the custom toString method to call.
     *
     * @return the toString method to call.
     */
    public ExecutableElement getToStringMethod() {
      return toStringMethod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return "CustomToStringStrategyDescriptor{"
          + "toStringMethod=" + StringUtils.quoted(this.toStringMethod)
          + '}';
    }
  }
}
