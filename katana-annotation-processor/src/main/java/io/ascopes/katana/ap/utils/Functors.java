package io.ascopes.katana.ap.utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.checkerframework.common.util.report.qual.ReportCreation;
import org.checkerframework.common.util.report.qual.ReportInherit;

/**
 * Functional programming helpers.
 * <p>
 * Due to the complex nature of these functions, please keep them documented.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@ReportCreation
@ReportInherit
public final class Functors {

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

  /**
   * Collector that collects keys and values to a sorted map.
   *
   * @param keyMapper     the key mapper.
   * @param valueMapper   the value mapper.
   * @param keyComparator the algorithm to sort the keys with.
   * @param <T>           the input type.
   * @param <K>           the key type.
   * @param <V>           the value type.
   * @return the collector.
   */
  public static <T, K, V> Collector<T, ?, SortedMap<K, V>> toSortedMap(
      Function<T, K> keyMapper,
      Function<T, V> valueMapper,
      Comparator<K> keyComparator
  ) {
    return Collectors.toMap(
        keyMapper,
        valueMapper,
        (a, b) -> {
          throw new IllegalStateException("Element " + a + " has the same key as " + b);
        },
        () -> new TreeMap<>(keyComparator)
    );
  }
}
