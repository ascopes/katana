/*
 * Copyright (C) 2021 Ashley Scopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ascopes.katana.ap.builders;

import io.ascopes.katana.ap.attributes.AttributeDescriptor;
import io.ascopes.katana.ap.builders.Stages.DedicatedStage;
import io.ascopes.katana.ap.builders.Stages.FinalStage;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.types.ModelDescriptor;
import io.ascopes.katana.ap.utils.CollectionUtils;
import io.ascopes.katana.ap.utils.KatanaIterator;
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
   * @param modelDescriptor    the model to analyse.
   * @param requiredAttributes the attributes to mark as required.
   * @param strategy           the builder strategy to use.
   * @return the stages descriptor.
   */
  Stages create(
      ModelDescriptor modelDescriptor,
      SortedSet<AttributeDescriptor> requiredAttributes,
      BuilderStrategyDescriptor strategy
  ) {
    SortedSet<AttributeDescriptor> nonRequiredAttributeDescriptors = modelDescriptor
        .getAttributes()
        .stream()
        .filter(attr -> !requiredAttributes.contains(attr))
        .collect(CollectionUtils.toSortedSet(AttributeDescriptor::getIdentifier));

    String finalStageName = this.getFinalStageName(strategy);
    FinalStageImpl finalStage = new FinalStageImpl(finalStageName, nonRequiredAttributeDescriptors);

    List<DedicatedStage> dedicatedStages = new ArrayList<>();

    // Freeze once here, this will prevent duplicating this as freezeList internally returns
    // the input if it is already frozen (see JDK 8 impl).
    List<DedicatedStage> unmodifiableStagesRef = CollectionUtils.freezeList(dedicatedStages);

    for (AttributeDescriptor requiredAttributeDescriptor : requiredAttributes) {
      String name = this.getDedicatedStageNameFor(strategy, requiredAttributeDescriptor);
      dedicatedStages.add(new DedicatedStageImpl(
          name,
          unmodifiableStagesRef,
          requiredAttributeDescriptor,
          finalStage
      ));
    }

    this.logger.trace("Created {} dedicated stages for type-safe builder", dedicatedStages.size());

    return new StagesImpl(dedicatedStages, finalStage);
  }

  private String getDedicatedStageNameFor(BuilderStrategyDescriptor strategy,
      AttributeDescriptor attributeDescriptor) {
    // TODO(ascopes): allow customising this.
    return NamingUtils.toPascalCase(attributeDescriptor.getName()) + "AttrStage";
  }

  private String getFinalStageName(BuilderStrategyDescriptor strategy) {
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
    private final AttributeDescriptor attributeDescriptor;
    private final FinalStageImpl finalStage;

    private DedicatedStageImpl(
        String name,
        List<DedicatedStage> stages,
        AttributeDescriptor attributeDescriptor,
        FinalStageImpl finalStage
    ) {
      this.name = Objects.requireNonNull(name);
      this.stages = CollectionUtils.freezeList(stages);
      this.index = stages.size();
      this.attributeDescriptor = Objects.requireNonNull(attributeDescriptor);
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
    public AttributeDescriptor getAttribute() {
      return this.attributeDescriptor;
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
    private final SortedSet<AttributeDescriptor> attributeDescriptors;

    private FinalStageImpl(String name, SortedSet<AttributeDescriptor> attributeDescriptors) {
      this.name = Objects.requireNonNull(name);
      this.attributeDescriptors = CollectionUtils.freezeSortedSet(attributeDescriptors);
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
    public SortedSet<AttributeDescriptor> getAttributes() {
      return this.attributeDescriptors;
    }
  }
}
