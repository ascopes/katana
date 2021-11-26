package io.ascopes.katana.ap.codegen.components;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.ascopes.katana.ap.codegen.init.InitTracker;
import io.ascopes.katana.ap.codegen.init.InitTrackerFactory;
import io.ascopes.katana.ap.descriptors.BuilderStrategy;
import io.ascopes.katana.ap.descriptors.Model;
import io.ascopes.katana.ap.utils.Logger;
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

  public BuilderFactory() {
    this.initTrackerFactory = new InitTrackerFactory();
    this.logger = new Logger();
  }

  public BuilderComponents create(Model model, BuilderStrategy strategy) {
    InitTracker initTracker = this.initTrackerFactory.createTracker(model.getAttributes());

    // TODO(ascopes): implement.
    throw new UnsupportedOperationException();
  }

  public final static class BuilderComponents {

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

    public MethodSpec getBuilderInitializer() {
      return this.builderInitializer;
    }

    public MethodSpec getBuilderConstructor() {
      return this.builderConstructor;
    }

    @MaybePresent
    public Optional<MethodSpec> getToBuilderMethod() {
      return Optional.ofNullable(this.toBuilderMethod);
    }

    public TypeSpec getBuilderType() {
      return this.builderType;
    }
  }
}
