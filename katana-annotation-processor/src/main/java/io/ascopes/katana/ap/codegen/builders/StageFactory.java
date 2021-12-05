package io.ascopes.katana.ap.codegen.builders;

import io.ascopes.katana.ap.codegen.builders.Stages.DedicatedStage;
import io.ascopes.katana.ap.codegen.builders.Stages.FinalStage;
import io.ascopes.katana.ap.descriptors.Attribute;
import io.ascopes.katana.ap.descriptors.BuilderStrategy;
import io.ascopes.katana.ap.descriptors.Model;
import io.ascopes.katana.ap.iterators.KatanaIterator;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.utils.CollectionUtils;
import io.ascopes.katana.ap.utils.NamingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;


/**
 * Factory that can produce a chain of stages to represent the stages to apply to a staged builder.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
final class StageFactory {

  private final Logger logger;

  StageFactory() {
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  /**
   * Create the stages descriptor for a type-safe builder.
   *
   * @param model              the model to analyse.
   * @param requiredAttributes the attributes to mark as required.
   * @param strategy           the builder strategy to use.
   * @return the stages descriptor.
   */
  Stages create(Model model, SortedSet<Attribute> requiredAttributes, BuilderStrategy strategy) {
    SortedSet<Attribute> nonRequiredAttributes = model
        .getAttributes()
        .stream()
        .filter(attr -> !requiredAttributes.contains(attr))
        .collect(CollectionUtils.toSortedSet(Attribute::getIdentifier));

    String finalStageName = this.getFinalStageName(strategy);
    FinalStageImpl finalStage = new FinalStageImpl(finalStageName, nonRequiredAttributes);

    List<DedicatedStage> dedicatedStages = new ArrayList<>();

    // Freeze once here, this will prevent duplicating this as freezeList internally returns
    // the input if it is already frozen (see JDK 8 impl).
    List<DedicatedStage> unmodifiableStagesRef = CollectionUtils.freezeList(dedicatedStages);

    for (Attribute requiredAttribute : requiredAttributes) {
      String name = this.getDedicatedStageNameFor(strategy, requiredAttribute);
      dedicatedStages.add(new DedicatedStageImpl(
          name,
          unmodifiableStagesRef,
          requiredAttribute,
          finalStage
      ));
    }

    this.logger.trace("Created {} dedicated stages for type-safe builder", dedicatedStages.size());

    return new StagesImpl(dedicatedStages, finalStage);
  }

  private String getDedicatedStageNameFor(BuilderStrategy strategy, Attribute attribute) {
    // TODO(ascopes): allow customising this.
    return NamingUtils.toPascalCase(attribute.getName()) + "AttrStage";
  }

  private String getFinalStageName(BuilderStrategy strategy) {
    // TODO(ascopes): allow customising this.
    return "BuilderFinalStage";
  }

  /**
   * Implementation of stage groups.
   */
  private static final class StagesImpl implements Stages {

    private final List<DedicatedStage> dedicatedStages;
    private final FinalStage finalStage;

    private StagesImpl(List<DedicatedStage> dedicatedStages, FinalStage finalStage) {
      this.dedicatedStages = CollectionUtils.freezeList(dedicatedStages);
      this.finalStage = Objects.requireNonNull(finalStage);
    }

    @Override
    public KatanaIterator<DedicatedStage> dedicatedStageIterator() {
      return KatanaIterator.decorate(this.dedicatedStages.iterator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FinalStage getFinalStage() {
      return this.finalStage;
    }
  }

  /**
   * Implementation of a dedicated stage.
   */
  private static final class DedicatedStageImpl implements DedicatedStage {

    private final String name;
    private final List<DedicatedStage> stages;
    private final int index;
    private final Attribute attribute;
    private final FinalStageImpl finalStage;

    private DedicatedStageImpl(
        String name,
        List<DedicatedStage> stages,
        Attribute attribute,
        FinalStageImpl finalStage
    ) {
      this.name = Objects.requireNonNull(name);
      this.stages = CollectionUtils.freezeList(stages);
      this.index = stages.size();
      this.attribute = Objects.requireNonNull(attribute);
      this.finalStage = Objects.requireNonNull(finalStage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
      return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Attribute getAttribute() {
      return this.attribute;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<? extends DedicatedStage> getNextDedicatedStage() {
      if (this.index < this.stages.size() - 1) {
        return Optional.of(this.stages.get(this.index + 1));
      }
      return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FinalStage getFinalStage() {
      return this.finalStage;
    }
  }

  /**
   * Implementation of a final stage.
   */
  private static final class FinalStageImpl implements FinalStage {

    private final String name;
    private final SortedSet<Attribute> attributes;

    private FinalStageImpl(String name, SortedSet<Attribute> attributes) {
      this.name = Objects.requireNonNull(name);
      this.attributes = CollectionUtils.freezeSortedSet(attributes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
      return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Attribute> getAttributes() {
      return this.attributes;
    }
  }
}
