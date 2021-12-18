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
import io.ascopes.katana.ap.utils.KatanaIterator;
import java.util.Optional;
import java.util.SortedSet;


/**
 * A linked list of dedicated stages to use in a staged builder, and a final stage that provides the
 * ability to set any attributes that are optional.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
interface Stages {

  /**
   * Get an iterator across each dedicated stage.
   *
   * @return the iterator.
   */
  KatanaIterator<DedicatedStage> dedicatedStageIterator();

  /**
   * Get the final stage holding all optional attributes.
   *
   * @return the final stage.
   */
  FinalStage getFinalStage();

  /**
   * Abstract definition of any stage.
   */
  interface Stage {

    /**
     * Get the name of the stage.
     *
     * @return the stage name.
     */
    String getName();
  }

  /**
   * Abstract definition of a stage that is dedicated to a given attribute in a staged builder.
   */
  interface DedicatedStage extends Stage {

    /**
     * Get the attribute that applies to this stage.
     *
     * @return the attribute.
     */
    AttributeDescriptor getAttribute();

    /**
     * Get the optional next stage.
     *
     * @return the next stage if set, or an empty optional if unset.
     */
    Optional<? extends DedicatedStage> getNextDedicatedStage();

    /**
     * Get the final stage.
     *
     * @return the final stage.
     */
    FinalStage getFinalStage();

    /**
     * Get the next stage.
     *
     * <p>This may be a dedicated stage or a final stage.
     *
     * @return the next stage.
     */
    default Stage getNextStage() {
      return this.getNextDedicatedStage()
          .map(Stage.class::cast)
          .orElseGet(this::getFinalStage);
    }
  }

  /**
   * Abstract definition of a final stage where all optional attributes can be provided.
   */
  interface FinalStage extends Stage {

    /**
     * Get all optional attributes in the stage.
     *
     * @return all optional attributes in the stage.
     */
    SortedSet<AttributeDescriptor> getAttributes();
  }
}
