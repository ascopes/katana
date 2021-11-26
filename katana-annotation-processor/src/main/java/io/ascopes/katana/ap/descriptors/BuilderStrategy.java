package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.utils.ObjectBuilder;
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

  public String getName() {
    return this.name;
  }

  public boolean isToBuilderEnabled() {
    return this.toBuilderEnabled;
  }

  public static Builder builder() {
    return new Builder();
  }

  // This is a BuilderStrategyBuilder, who says I can't use annoying names in my code?
  @MustCall("build")
  public static final class Builder implements ObjectBuilder<BuilderStrategy> {

    private @MonotonicNonNull String name;
    private @MonotonicNonNull Boolean toBuilderEnabled;

    private Builder() {
    }

    public Builder name(String name) {
      this.name = Objects.requireNonNull(name);
      return this;
    }

    public Builder toBuilderEnabled(Boolean toBuilderEnabled) {
      this.toBuilderEnabled = Objects.requireNonNull(toBuilderEnabled);
      return this;
    }

    @Override
    public BuilderStrategy build() {
      return new BuilderStrategy(this);
    }
  }
}
