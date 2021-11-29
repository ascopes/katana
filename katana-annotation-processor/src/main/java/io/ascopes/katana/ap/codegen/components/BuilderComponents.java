package io.ascopes.katana.ap.codegen.components;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.ascopes.katana.ap.utils.ObjectBuilder;
import java.util.Objects;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.optional.qual.MaybePresent;

/**
 * Collection of source code fragments for implementing builders.
 */
public final class BuilderComponents {

  private final MethodSpec builderInitializer;
  private final MethodSpec builderConstructor;
  private final TypeSpec builderType;
  private final @Nullable MethodSpec toBuilderMethod;

  private BuilderComponents(Builder builder) {
    this.builderInitializer = Objects.requireNonNull(builder.builderInitializer);
    this.builderConstructor = Objects.requireNonNull(builder.builderConstructor);
    this.builderType = Objects.requireNonNull(builder.builderType);
    this.toBuilderMethod = builder.toBuilderMethod;
  }

  /**
   * The static method that returns a new builder.
   *
   * @return the builder creation method.
   */
  public MethodSpec getBuilderInitializer() {
    return this.builderInitializer;
  }

  /**
   * The private constructor that consumes a builder and initializes the model type.
   *
   * @return the constructor.
   */
  public MethodSpec getBuilderConstructor() {
    return this.builderConstructor;
  }

  /**
   * The method to convert an existing model back into a builder as a copy.
   *
   * @return the method to convert the model instance to a builder, if enabled. If disabled, an
   *     empty optional is returned.
   */
  @MaybePresent
  public Optional<MethodSpec> getToBuilderMethod() {
    return Optional.ofNullable(this.toBuilderMethod);
  }

  /**
   * The type spec for the builder implementation.
   *
   * @return the builder type spec.
   */
  public TypeSpec getBuilderType() {
    return this.builderType;
  }

  /**
   * Create a new builder components builder.
   *
   * @return the builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for builder components objects.
   */
  public static class Builder implements ObjectBuilder<BuilderComponents> {

    private @MonotonicNonNull MethodSpec builderInitializer;
    private @MonotonicNonNull MethodSpec builderConstructor;
    private @MonotonicNonNull TypeSpec builderType;
    private @Nullable MethodSpec toBuilderMethod;

    private Builder() {
      // private init only.
    }

    /**
     * Set the builder-initializer static method.
     *
     * @param builderInitializer the builder-initializer static method.
     * @return this builder.
     */
    public Builder builderInitializer(MethodSpec builderInitializer) {
      this.builderInitializer = Objects.requireNonNull(builderInitializer);
      return this;
    }

    /**
     * Set the builder-consuming model constructor.
     *
     * @param builderConstructor the builder-consuming model constructor.
     * @return this builder.
     */
    public Builder builderConstructor(MethodSpec builderConstructor) {
      this.builderConstructor = Objects.requireNonNull(builderConstructor);
      return this;
    }

    /**
     * Set the builder type.
     *
     * @param builderType the builder type spec.
     * @return this builder.
     */
    public Builder builderType(TypeSpec builderType) {
      this.builderType = Objects.requireNonNull(builderType);
      return this;
    }

    /**
     * Set the toBuilderMethod.
     *
     * @param toBuilderMethod the nullable toBuilderMethod to set.
     * @return this builder.
     */
    public Builder toBuilderMethod(@Nullable MethodSpec toBuilderMethod) {
      this.toBuilderMethod = toBuilderMethod;
      return this;
    }

    /**
     * Build this builder and return the builder components.
     *
     * @return the builder components.
     */
    @Override
    public BuilderComponents build() {
      return new BuilderComponents(this);
    }
  }
}
