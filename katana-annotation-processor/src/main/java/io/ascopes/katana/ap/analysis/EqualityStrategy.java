package io.ascopes.katana.ap.analysis;

import io.ascopes.katana.ap.utils.StringUtils;
import java.util.Objects;
import javax.lang.model.element.ExecutableElement;

/**
 * Equality checking strategy.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public abstract class EqualityStrategy {

  private EqualityStrategy() {
    // sealed class.
  }

  /**
   * A strategy for generating a default implementation where attributes may be explicitly included
   * or excluded.
   */
  public static final class GeneratedEqualityStrategy extends EqualityStrategy {

    private final boolean includeAll;

    /**
     * Initialize this strategy.
     *
     * @param includeAll true if the strategy includes all attributes by default, or false if it
     *                   excludes all attributes by default.
     */
    public GeneratedEqualityStrategy(boolean includeAll) {
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
      return "GeneratedEqualityStrategy{"
          + "includeAll=" + this.includeAll
          + '}';
    }
  }

  /**
   * A strategy for using a custom implementation of both equals and hashCode, which will be user
   * provided.
   */
  public static final class CustomEqualityStrategy extends EqualityStrategy {

    private final ExecutableElement equalsMethod;
    private final ExecutableElement hashCodeMethod;

    /**
     * Initialize this strategy.
     *
     * @param equalsMethod   the equals method to call.
     * @param hashCodeMethod the hash code method to call.
     */
    public CustomEqualityStrategy(
        ExecutableElement equalsMethod,
        ExecutableElement hashCodeMethod
    ) {
      this.equalsMethod = Objects.requireNonNull(equalsMethod);
      this.hashCodeMethod = Objects.requireNonNull(hashCodeMethod);
    }

    /**
     * Get the custom equals method to call.
     *
     * @return the equals code method.
     */
    public ExecutableElement getEqualsMethod() {
      return this.equalsMethod;
    }

    /**
     * Get the custom hash code method to call.
     *
     * @return the hash code method.
     */
    public ExecutableElement getHashCodeMethod() {
      return this.hashCodeMethod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return "CustomEqualityStrategy{"
          + "equalsMethod=" + StringUtils.quoted(this.equalsMethod)
          + "hashCodeMethod=" + StringUtils.quoted(this.hashCodeMethod)
          + '}';
    }
  }
}
