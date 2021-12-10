package io.ascopes.katana.ap.builders;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Simple builder factory implementation that does not provide any checking of attribute
 * initialization.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class SimpleBuilderFactory extends AbstractBuilderFactory<@Nullable Void> {
  // Currently we do not override any default behaviours for this.
}
