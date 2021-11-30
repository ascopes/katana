package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import io.ascopes.katana.ap.utils.Result;


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
   * @return the strategy, an ignored result if builders are disabled, or a failed result if we
   *     could not parse anything.
   */
  public Result<BuilderStrategy> create(SettingsCollection settings) {
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
    if (!settings.getBuilder().getValue()) {
      return Result.ignore();
    }

    BuilderStrategy builderStrategy = BuilderStrategy
        .builder()
        .builderTypeName(settings.getBuilderTypeName().getValue())
        .toBuilderMethodEnabled(settings.getToBuilderMethodEnabled().getValue())
        .toBuilderMethodName(settings.getToBuilderMethodName().getValue())
        .builderMethodName(settings.getBuilderMethodName().getValue())
        .buildMethodName(settings.getBuildMethodName().getValue())
        .build();

    return Result.ok(builderStrategy);
  }
}
