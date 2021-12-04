package io.ascopes.katana.ap.codegen.builders;

import io.ascopes.katana.ap.codegen.TypeSpecMembers;
import io.ascopes.katana.ap.descriptors.BuilderStrategy;
import io.ascopes.katana.ap.descriptors.Model;

/**
 * Definition of a factory that produces builders to use.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public interface BuilderFactory {

  /**
   * Create the builder components to add to the generated model.
   *
   * @param model    the model to build the builder components for.
   * @param strategy the strategy to use for generating the builder.
   * @return the members to add to the generated model.
   */
  TypeSpecMembers create(Model model, BuilderStrategy strategy);
}
