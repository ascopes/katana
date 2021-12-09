package io.ascopes.katana.ap.builders;

import io.ascopes.katana.ap.attributes.AttributeDescriptor;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.types.ModelDescriptor;
import io.ascopes.katana.ap.types.TypeSpecMembers;
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
   * Bridge for {@link #create(ModelDescriptor, BuilderStrategyDescriptor, Void)}.
   */
  public TypeSpecMembers create(ModelDescriptor modelDescriptor,
      BuilderStrategyDescriptor strategy) {
    return this.create(modelDescriptor, strategy, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TypeSpecMembers create(
      ModelDescriptor model,
      BuilderStrategyDescriptor strategy,
      @Nullable Void context
  ) {
    //noinspection ConstantConditions
    assert context == null : "Unexpected context parameter passed";

    switch (strategy.getBuilderType()) {

      case SIMPLE:
        this.logger.trace("Creating simple unchecked builder");
        return this.simpleBuilderFactory.create(model, strategy, context);

      case RUNTIME_CHECKED: {
        this.logger.trace("Creating runtime initialization-checked builder");
        SortedSet<AttributeDescriptor> requiredAttrs = this.requiredAttributes(model);
        InitTracker initTracker = this.initTrackerFactory.create(requiredAttrs);
        return this.runtimeCheckedBuilderFactory.create(model, strategy, initTracker);
      }

      case TYPESAFE: {
        this.logger.trace("Creating typesafe initialization-checked builder");
        SortedSet<AttributeDescriptor> requiredAttrs = this.requiredAttributes(model);
        Stages stages = this.stageFactory.create(model, requiredAttrs, strategy);
        return this.compileCheckedBuilderFactory.create(model, strategy, stages);
      }

      default:
        throw new UnsupportedOperationException("Unsupported strategy " + strategy);
    }
  }

  private SortedSet<AttributeDescriptor> requiredAttributes(ModelDescriptor modelDescriptor) {
    // TODO(ascopes): determine which attributes are mandatory.
    return modelDescriptor
        .getAttributes()
        .stream()
        .collect(CollectionUtils.toSortedSet(AttributeDescriptor::getIdentifier));
  }
}

