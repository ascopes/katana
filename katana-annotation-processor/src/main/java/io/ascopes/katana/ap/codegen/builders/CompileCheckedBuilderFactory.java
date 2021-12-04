package io.ascopes.katana.ap.codegen.builders;

import io.ascopes.katana.ap.codegen.TypeSpecMembers;
import io.ascopes.katana.ap.descriptors.BuilderStrategy;
import io.ascopes.katana.ap.descriptors.Model;

/**
 * Simple builder factory implementation that provides initialization checking at compile time
 * through the use of a staged builder.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
final class CompileCheckedBuilderFactory implements BuilderFactory {

  /**
   * {@inheritDoc}
   */
  @Override
  public TypeSpecMembers create(Model model, BuilderStrategy strategy) {
    // TODO(ascopes): implement this.
    throw new UnsupportedOperationException("Not yet supported!");
  }
}
