package io.ascopes.katana.ap.utils;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Functional programming helpers.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public abstract class Functors {

  private Functors() {
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
   * Flat map operation that applies to a stream of Result objects and returns a stream of all
   * values that were OK. Any failed or ignored results get discarded.
   *
   * @param <T> the type within the results.
   * @return the function to apply.
   */
  public static <T> Function<Result<T>, Stream<T>> removeNonOkResults() {
    return opt -> opt
        .ifOkMap(Stream::of)
        .elseGet(Stream::empty);
  }

  /**
   * Flat map operation that flattens a stream of streams into a single stream.
   *
   * @param <T> the elements in each stream.
   * @return the function to apply.
   */
  public static <T> Function<Stream<T>, Stream<T>> flattenStream() {
    // This is more explicit than just adding Function.identity and not immediately realising that
    // it is being used to flatten a stream of streams into a stream. JVM will likely JIT this out
    // anyway.
    return Function.identity();
  }

  /**
   * Flat map operation that flattens a stream of collections into a single stream.
   *
   * @param <C> the collection type.
   * @param <T> the elements in each collection.
   * @return the function to apply.
   */
  public static <C extends Collection<T>, T> Function<C, Stream<T>> flattenCollection() {
    // This is more explicit than just adding Collection.stream and not immediately realising that
    // it is being used to flatten a stream of collections into a stream. JVM will likely JIT this
    // out anyway.
    return Collection::stream;
  }
}
