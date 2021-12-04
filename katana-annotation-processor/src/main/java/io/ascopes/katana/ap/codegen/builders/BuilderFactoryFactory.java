package io.ascopes.katana.ap.codegen.builders;

import io.ascopes.katana.ap.descriptors.BuilderStrategy;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;

/**
 * Factory for creating the builder generation logic to use for a model.
 *
 * <p>This exists to provide a singular interface to cater for multiple types of builder,
 * such as type-safe staged, runtime initialization checking, and
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class BuilderFactoryFactory {

  private final Logger logger;
  private final BuilderFactory simpleBuilderFactory;
  private final BuilderFactory runtimeCheckedBuilderFactory;
  private final BuilderFactory compileCheckedBuilderFactory;

  /**
   * Initialize this factory.
   */
  public BuilderFactoryFactory() {
    InitTrackerFactory initTrackerFactory = new InitTrackerFactory();
    this.logger = LoggerFactory.loggerFor(this.getClass());
    this.simpleBuilderFactory = new SimpleBuilderFactory();
    this.runtimeCheckedBuilderFactory = new RuntimeCheckedBuilderFactory(initTrackerFactory);
    this.compileCheckedBuilderFactory = new CompileCheckedBuilderFactory();
  }

  /**
   * Create a builder factory for the given strategy.
   *
   * @param strategy the strategy to use to create the builder factory.
   * @return the generated builder factory to use.
   */
  public BuilderFactory create(BuilderStrategy strategy) {
    switch (strategy.getBuilderInitCheck()) {
      case NONE:
        this.logger.trace("Will use an SimpleBuilderFactory to generate the builder");
        return this.simpleBuilderFactory;
      case RUNTIME:
        this.logger.trace("Will use a RuntimeCheckedBuilderFactory to generate the builder");
        return this.runtimeCheckedBuilderFactory;
      case TYPESAFE:
        this.logger.trace("Will use a CompileCheckedBuilderFactory to generate the builder");
        return this.compileCheckedBuilderFactory;
      default:
        throw new UnsupportedOperationException(
            "Unsupported builder initialization check " + strategy.getBuilderInitCheck()
        );
    }
  }
}

