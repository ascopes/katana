package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.utils.ObjectBuilder;
import io.ascopes.katana.ap.utils.StringUtils;
import java.util.Objects;
import org.checkerframework.checker.mustcall.qual.MustCall;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

/**
 * Strategy for making builders.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class BuilderStrategy {

  private final String name;
  private final boolean toBuilderEnabled;

  private BuilderStrategy(Builder builder) {
    this.name = Objects.requireNonNull(builder.name);
    this.toBuilderEnabled = Objects.requireNonNull(builder.toBuilderEnabled);
  }

  /**
   * Get the builder type name.
   *
   * @return the name of the builder type.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Determine whether the {@code toBuilder} method is enabled or not.
   *
   * @return true if the {@code toBuilder} method is enabled, or false if it is disabled.
   */
  public boolean isToBuilderEnabled() {
    return this.toBuilderEnabled;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "BuilderStrategy{"
        + "name=" + StringUtils.quoted(this.name) + ", "
        + "toBuilderEnabled=" + this.toBuilderEnabled
        + '}';
  }

  /**
   * Create a new BuilderStrategy builder.
   *
   * @return the new builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * A builder for {@link BuilderStrategy} descriptors..
   */
  @MustCall("build")
  public static final class Builder implements ObjectBuilder<BuilderStrategy> {

    private @MonotonicNonNull String name;
    private @MonotonicNonNull Boolean toBuilderEnabled;
    // TODO(ascopes): toBuilderMethodName?

    private Builder() {
    }

    /**
     * Set the name for the builder type.
     *
     * @param name the name of the builder type.
     * @return this builder.
     */
    public Builder name(String name) {
      this.name = Objects.requireNonNull(name);
      return this;
    }

    /**
     * Set whether the toBuilder method is enabled or not.
     *
     * @param toBuilderEnabled true if the toBuilder method is enabled, false otherwise.
     * @return this builder.
     */
    public Builder toBuilderEnabled(Boolean toBuilderEnabled) {
      this.toBuilderEnabled = Objects.requireNonNull(toBuilderEnabled);
      return this;
    }

    /**
     * Construct the BuilderStrategy from this builder.
     *
     * @return the constructed BuilderStrategy.
     */
    @Override
    public BuilderStrategy build() {
      return new BuilderStrategy(this);
    }
  }
}
