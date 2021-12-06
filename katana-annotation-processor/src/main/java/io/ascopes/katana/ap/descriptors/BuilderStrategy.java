package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.annotations.Builder;
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

  private final String builderClassName;
  private final boolean toBuilderMethodEnabled;
  private final String toBuilderMethodName;
  private final String builderMethodName;
  private final String buildMethodName;
  private final Builder builderType;

  private BuilderStrategy(BuilderStrategyBuilder builder) {
    this.builderClassName = Objects.requireNonNull(builder.builderClassName);
    this.toBuilderMethodEnabled = Objects.requireNonNull(builder.toBuilderMethodEnabled);
    this.toBuilderMethodName = Objects.requireNonNull(builder.toBuilderMethodName);
    this.builderMethodName = Objects.requireNonNull(builder.builderMethodName);
    this.buildMethodName = Objects.requireNonNull(builder.buildMethodName);
    this.builderType = Objects.requireNonNull(builder.builderType);
  }

  /**
   * Get the builder type name.
   *
   * @return the name of the builder type.
   */
  public String getBuilderClassName() {
    return this.builderClassName;
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
   * Get the type of builder to use.
   *
   * @return the type of builder to use.
   */
  public Builder getBuilderType() {
    return this.builderType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "BuilderStrategy{"
        + "name=" + StringUtils.quoted(this.builderClassName) + ", "
        + "toBuilderEnabled=" + this.toBuilderMethodEnabled + ", "
        + "toBuilderMethodName=" + StringUtils.quoted(this.toBuilderMethodName) + ", "
        + "builderMethodName=" + StringUtils.quoted(this.builderMethodName) + ", "
        + "buildMethodName=" + StringUtils.quoted(this.buildMethodName) + ", "
        + "builderInitCheck=" + this.builderType
        + '}';
  }

  /**
   * Create a new BuilderStrategy builder.
   *
   * @return the new builder.
   */
  public static BuilderStrategyBuilder builder() {
    return new BuilderStrategyBuilder();
  }

  /**
   * A builder for {@link BuilderStrategy} descriptors..
   */
  @MustCall("build")
  public static final class BuilderStrategyBuilder implements ObjectBuilder<BuilderStrategy> {

    private @MonotonicNonNull String builderClassName;
    private @MonotonicNonNull Boolean toBuilderMethodEnabled;
    private @MonotonicNonNull String toBuilderMethodName;
    private @MonotonicNonNull String buildMethodName;
    private @MonotonicNonNull String builderMethodName;
    private @MonotonicNonNull Builder builderType;

    private BuilderStrategyBuilder() {
    }

    /**
     * Set the name for the builder type.
     *
     * @param builderClassName the name of the builder type.
     * @return this builder.
     */
    public BuilderStrategyBuilder builderClassName(String builderClassName) {
      this.builderClassName = Objects.requireNonNull(builderClassName);
      return this;
    }

    /**
     * Set whether the toBuilder method is enabled or not.
     *
     * @param toBuilderMethodEnabled true if the toBuilder method is enabled, false otherwise.
     * @return this builder.
     */
    public BuilderStrategyBuilder toBuilderMethodEnabled(Boolean toBuilderMethodEnabled) {
      this.toBuilderMethodEnabled = Objects.requireNonNull(toBuilderMethodEnabled);
      return this;
    }

    /**
     * Set the toBuilder method name.
     *
     * @param toBuilderMethodName the name of the toBuilder method.
     * @return this builder.
     */
    public BuilderStrategyBuilder toBuilderMethodName(String toBuilderMethodName) {
      this.toBuilderMethodName = Objects.requireNonNull(toBuilderMethodName);
      return this;
    }

    /**
     * Set the builder static method name.
     *
     * @param builderMethodName the name of the builder method.
     * @return this builder.
     */
    public BuilderStrategyBuilder builderMethodName(String builderMethodName) {
      this.builderMethodName = Objects.requireNonNull(builderMethodName);
      return this;
    }

    /**
     * Set the toBuilder method name.
     *
     * @param buildMethodName the name of the build method.
     * @return this builder.
     */
    public BuilderStrategyBuilder buildMethodName(String buildMethodName) {
      this.buildMethodName = Objects.requireNonNull(buildMethodName);
      return this;
    }

    /**
     * Set the type of builder to use.
     *
     * @param builderType the type of builder to use.
     * @return this builder.
     */
    public BuilderStrategyBuilder builderType(Builder builderType) {
      this.builderType = Objects.requireNonNull(builderType);
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
