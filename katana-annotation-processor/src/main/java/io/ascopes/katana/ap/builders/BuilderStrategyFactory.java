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

import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import java.util.Optional;


/**
 * Factory for BuilderStrategy types.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class BuilderStrategyFactory {

  /**
   * Create a builder strategy from the given settings.
   *
   * @param settings the settings to use.
   * @return the strategy, or an empty optional if ignored.
   */
  public Optional<BuilderStrategyDescriptor> create(SettingsCollection settings) {
    // Edge case I probably won't account for.
    //
    // I wake up one day and decide "hey, lets make a model that has a builder, but also make it
    // so the model only has one field, which has the type of the builder".
    // Then I decide to also make an all-arguments constructor.
    //
    // There is literally zero reason anyone would reasonably do this, I think. If there is,
    // they can open an issue and explain it to me, and then I will probably go and scratch my
    // head over how to best deal with this for a few days, then alter the code in this method
    // to do something.... probably.
    if (!settings.getBuilder().getValue().isDisabled()) {
      return Optional.empty();
    }

    BuilderStrategyDescriptor builderStrategyDescriptor = BuilderStrategyDescriptor
        .builder()
        .builderClassName(settings.getBuilderClassName().getValue())
        .toBuilderMethodEnabled(settings.getToBuilderMethodEnabled().getValue())
        .toBuilderMethodName(settings.getToBuilderMethodName().getValue())
        .builderMethodName(settings.getInitBuilderMethodName().getValue())
        .buildMethodName(settings.getBuilderBuildMethodName().getValue())
        .builderType(settings.getBuilder().getValue())
        .build();

    return Optional.of(builderStrategyDescriptor);
  }
}
