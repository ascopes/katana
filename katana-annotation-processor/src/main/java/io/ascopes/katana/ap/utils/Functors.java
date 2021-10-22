package io.ascopes.katana.ap.utils;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Functional helper methods.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public abstract class Functors {
  private Functors() {
    // Static-only class.
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Flat map operation that applies to a stream of Optional objects and returns a stream of
   * non-optional objects. Any empty optionals get discarded.
   *
   * @param <T> the type within the optionals.
   * @return the function to apply.
   */
  public static <T> Function<Optional<T>, Stream<T>> removeEmpties() {
    return opt -> opt
        .map(Stream::of)
        .orElseGet(Stream::empty);
  }
}
