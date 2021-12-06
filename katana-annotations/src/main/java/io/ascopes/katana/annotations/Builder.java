package io.ascopes.katana.annotations;

import io.ascopes.katana.annotations.internal.Feature;

/**
 * Initialization checking mode for builders.
 *
 * <p>The purpose of this is to determine if attributes have been specified explicitly or not
 * when using a generated builder.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public enum Builder implements Feature {
  /**
   * The builder is disabled.
   */
  DISABLED {
    @Override
    public boolean isDisabled() {
      return true;
    }
  },

  /**
   * A regular builder that does not check that required attributes are initialized.
   */
  SIMPLE,

  /**
   * Builder that implements checks for uninitialized attributes by tracking the methods that
   * are called at runtime.
   *
   * <p>Attempting to build the builder with uninitialized attributes that are required will
   * result in an {@link IllegalStateException} being thrown at runtime.
   */
  RUNTIME_CHECKED,

  /**
   * Builder that is staged (type-safe). This uses interfaces for every required attribute to
   * enforce that the attributes are initialized at compile time. Failure to initialize a
   * required attribute will result in a compilation error.
   */
  TYPESAFE,
}
