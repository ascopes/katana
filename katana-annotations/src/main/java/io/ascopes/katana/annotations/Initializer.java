package io.ascopes.katana.annotations;

public enum Initializer {
  /**
   * Implement a constructor that takes all attributes as parameters.
   */
  ALL_ATTRS_CONSTRUCTOR,

  /**
   * Implement a builder class, and provide a {@code toBuilder()} method on the implementation.
   */
  BUILDER,

  /**
   * Implement a constructor that takes an instance of the interface as a parameter and performs
   * a shallow copy.
   */
  COPY_CONSTRUCTOR,

  /**
   * Implement a constructor that takes no arguments.
   */
  NO_ATTRS_CONSTRUCTOR,
}
