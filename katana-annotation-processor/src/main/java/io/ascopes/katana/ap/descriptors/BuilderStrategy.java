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

  private final String builderTypeName;
  private final boolean toBuilderMethodEnabled;
  private final String toBuilderMethodName;
  private final String builderMethodName;
  private final String buildMethodName;

  private BuilderStrategy(Builder builder) {
    this.builderTypeName = Objects.requireNonNull(builder.builderTypeName);
    this.toBuilderMethodEnabled = Objects.requireNonNull(builder.toBuilderMethodEnabled);
    this.toBuilderMethodName = Objects.requireNonNull(builder.toBuilderMethodName);
    this.builderMethodName = Objects.requireNonNull(builder.builderMethodName);
    this.buildMethodName = Objects.requireNonNull(builder.buildMethodName);
  }

  /**
   * Get the builder type name.
   *
   * @return the name of the builder type.
   */
  public String getBuilderTypeName() {
    return this.builderTypeName;
  }

  /**
   * Determine whether the {@code toBuilder} method is enabled or not.
   *
   * @return true if the {@code toBuilder} method is enabled, or false if it is disabled.
   */
  public boolean isToBuilderMethodEnabled() {
    return this.toBuilderMethodEnabled;
  }

  /**
   * Get the toBuilder method name.
   *
   * @return the name of the toBuilder method.
   */
  public String getToBuilderMethodName() {
    return this.toBuilderMethodName;
  }

  /**
   * Get the builder static method name.
   *
   * @return the name of the builder method.
   */
  public String getBuilderMethodName() {
    return this.builderMethodName;
  }

  /**
   * Get the build method name.
   *
   * @return the name of the build method.
   */
  public String getBuildMethodName() {
    return this.buildMethodName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "BuilderStrategy{"
        + "name=" + StringUtils.quoted(this.builderTypeName) + ", "
        + "toBuilderEnabled=" + this.toBuilderMethodEnabled + ", "
        + "toBuilderMethodName=" + StringUtils.quoted(this.toBuilderMethodName) + ", "
        + "builderMethodName=" + StringUtils.quoted(this.builderMethodName) + ", "
        + "buildMethodName=" + StringUtils.quoted(this.buildMethodName)
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

    private @MonotonicNonNull String builderTypeName;
    private @MonotonicNonNull Boolean toBuilderMethodEnabled;
    private @MonotonicNonNull String toBuilderMethodName;
    private @MonotonicNonNull String buildMethodName;
    private @MonotonicNonNull String builderMethodName;

    private Builder() {
    }

    /**
     * Set the name for the builder type.
     *
     * @param builderTypeName the name of the builder type.
     * @return this builder.
     */
    public Builder builderTypeName(String builderTypeName) {
      this.builderTypeName = Objects.requireNonNull(builderTypeName);
      return this;
    }

    /**
     * Set whether the toBuilder method is enabled or not.
     *
     * @param toBuilderMethodEnabled true if the toBuilder method is enabled, false otherwise.
     * @return this builder.
     */
    public Builder toBuilderMethodEnabled(Boolean toBuilderMethodEnabled) {
      this.toBuilderMethodEnabled = Objects.requireNonNull(toBuilderMethodEnabled);
      return this;
    }

    /**
     * Set the toBuilder method name.
     *
     * @param toBuilderMethodName the name of the toBuilder method.
     * @return this builder.
     */
    public Builder toBuilderMethodName(String toBuilderMethodName) {
      this.toBuilderMethodName = Objects.requireNonNull(toBuilderMethodName);
      return this;
    }

    /**
     * Set the builder static method name.
     *
     * @param builderMethodName the name of the builder method.
     * @return this builder.
     */
    public Builder builderMethodName(String builderMethodName) {
      this.builderMethodName = Objects.requireNonNull(builderMethodName);
      return this;
    }

    /**
     * Set the toBuilder method name.
     *
     * @param buildMethodName the name of the build method.
     * @return this builder.
     */
    public Builder buildMethodName(String buildMethodName) {
      this.buildMethodName = Objects.requireNonNull(buildMethodName);
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
