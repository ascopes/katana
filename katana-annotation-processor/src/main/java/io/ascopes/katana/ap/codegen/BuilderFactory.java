package io.ascopes.katana.ap.codegen;

import io.ascopes.katana.ap.analysis.BuilderStrategy;
import io.ascopes.katana.ap.analysis.Model;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Definition of a factory that produces builders to use.
 *
 * @param <T> context information. May not be required by some implementations.
 * @author Ashley Scopes
 * @since 0.0.1
 */
interface BuilderFactory<@Nullable T> {

  /**
   * Create the builder components to add to the generated model.
   *
   * @param model    the model to build the builder components for.
   * @param strategy the strategy to use for generating the builder.
   * @param context  the context info to pass.
   * @return the members to add to the generated model.
   */
  TypeSpecMembers create(Model model, BuilderStrategy strategy, @Nullable T context);
}
