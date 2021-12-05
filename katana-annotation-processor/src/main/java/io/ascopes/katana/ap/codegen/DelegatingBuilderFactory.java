package io.ascopes.katana.ap.codegen;

import io.ascopes.katana.ap.descriptors.Attribute;
import io.ascopes.katana.ap.descriptors.BuilderStrategy;
import io.ascopes.katana.ap.descriptors.Model;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.utils.CollectionUtils;
import java.util.SortedSet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Factory for generating builder components which decides on the correct builder to produce by
 * delegating to another implementation internally.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class DelegatingBuilderFactory implements BuilderFactory<@Nullable Void> {

  private final Logger logger;
  private final InitTrackerFactory initTrackerFactory;
  private final StageFactory stageFactory;
  private final SimpleBuilderFactory simpleBuilderFactory;
  private final RuntimeCheckedBuilderFactory runtimeCheckedBuilderFactory;
  private final CompileCheckedBuilderFactory compileCheckedBuilderFactory;

  /**
   * Initialize this factory.
   */
  public DelegatingBuilderFactory() {
    this.logger = LoggerFactory.loggerFor(this.getClass());
    this.initTrackerFactory = new InitTrackerFactory();
    this.stageFactory = new StageFactory();
    this.simpleBuilderFactory = new SimpleBuilderFactory();
    this.runtimeCheckedBuilderFactory = new RuntimeCheckedBuilderFactory();
    this.compileCheckedBuilderFactory = new CompileCheckedBuilderFactory();
  }

  /**
   * Bridge for {@link #create(Model, BuilderStrategy, Void)}.
   */
  public TypeSpecMembers create(Model model, BuilderStrategy strategy) {
    return this.create(model, strategy, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TypeSpecMembers create(Model model, BuilderStrategy strategy, @Nullable Void context) {
    //noinspection ConstantConditions
    assert context == null : "Unexpected context parameter passed";

    switch (strategy.getBuilderInitCheck()) {

      case NONE:
        this.logger.trace("Creating simple unchecked builder");
        return this.simpleBuilderFactory.create(model, strategy, context);

      case RUNTIME: {
        this.logger.trace("Creating runtime initialization-checked builder");
        SortedSet<Attribute> requiredAttributes = this.requiredAttributes(model);
        InitTracker initTracker = this.initTrackerFactory.create(requiredAttributes);
        return this.runtimeCheckedBuilderFactory.create(model, strategy, initTracker);
      }

      case TYPESAFE: {
        this.logger.trace("Creating typesafe initialization-checked builder");
        SortedSet<Attribute> requiredAttributes = this.requiredAttributes(model);
        Stages stages = this.stageFactory.create(model, requiredAttributes, strategy);
        return this.compileCheckedBuilderFactory.create(model, strategy, stages);
      }

      default:
        throw new UnsupportedOperationException("Unsupported strategy " + strategy);
    }
  }

  private SortedSet<Attribute> requiredAttributes(Model model) {
    // TODO(ascopes): determine which attributes are mandatory.
    return model
        .getAttributes()
        .stream()
        .collect(CollectionUtils.toSortedSet(Attribute::getIdentifier));
  }
}

