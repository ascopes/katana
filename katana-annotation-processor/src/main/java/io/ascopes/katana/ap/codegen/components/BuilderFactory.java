package io.ascopes.katana.ap.codegen.components;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.ascopes.katana.ap.codegen.init.InitTracker;
import io.ascopes.katana.ap.codegen.init.InitTrackerFactory;
import io.ascopes.katana.ap.descriptors.BuilderStrategy;
import io.ascopes.katana.ap.descriptors.Model;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import java.util.Objects;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.optional.qual.MaybePresent;

/**
 * Factory for creating a builder and the associated conduit.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class BuilderFactory {

  private final InitTrackerFactory initTrackerFactory;
  private final Logger logger;

  /**
   * Initialize this factory.
   */
  public BuilderFactory() {
    this.initTrackerFactory = new InitTrackerFactory();
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  /**
   * Create a set of builder components used to define a builder and initialize the model from the
   * builder, using a given model.
   *
   * @param model    the model to generate the builder for.
   * @param strategy the strategy to use to create the builder.
   * @return the generated builder components.
   */
  public BuilderComponents create(Model model, BuilderStrategy strategy) {
    InitTracker initTracker = this.initTrackerFactory.createTracker(model.getAttributes());

    // TODO(ascopes): implement.
    throw new UnsupportedOperationException();
  }

  /**
   * Collection of source code fragments for implementing builders.
   */
  public static final class BuilderComponents {

    private final MethodSpec builderInitializer;
    private final MethodSpec builderConstructor;
    private final TypeSpec builderType;
    private final @Nullable MethodSpec toBuilderMethod;

    private BuilderComponents(
        MethodSpec builderInitializer,
        MethodSpec builderConstructor,
        TypeSpec builderType,
        MethodSpec toBuilderMethod
    ) {
      this.builderInitializer = Objects.requireNonNull(builderInitializer);
      this.builderConstructor = Objects.requireNonNull(builderConstructor);
      this.builderType = Objects.requireNonNull(builderType);
      this.toBuilderMethod = Objects.requireNonNull(toBuilderMethod);
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
  }
}
