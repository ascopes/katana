package io.ascopes.katana.ap.commons;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Functional helper methods.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public abstract class Streams {
  private Streams() {
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

  /**
   * Flat map operation that flattens a stream of streams into a single stream.
   *
   * @param <T> the elements in each stream.
   * @return the function to apply.
   */
  public static <T> Function<Stream<T>, Stream<T>> flatten() {
    // This is more explicit than just adding Function.identity and not immediately realising that
    // it is being used to flatten a stream of streams into a stream. JVM will likely JIT this out
    // anyway.
    return Function.identity();
  }
}
