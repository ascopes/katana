package io.ascopes.katana.ap.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A collector for results that aggregates the result state. A combiner is provided to combine a
 * stream of unwrapped OK results, and this is then wrapped in an OK result state if everything
 * succeeds. If a failure is detected, then collection goes no further, results are dropped, and an
 * error result is provided instead.
 * <p>
 * An example usage of this type is to provide the transformation from a Stream of {@code Result<T>}
 * as the input to a Result of {@code Collection<T>} as the output.
 *
 * @param <T> the type within the input results.
 * @param <C> the result of the input collector.
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class ResultCollector<T, C> implements
    Collector<Result<T>, Result<Collection<T>>, Result<C>> {

  private final Collector<T, ?, C> finalCollector;

  private ResultCollector(Collector<T, ?, C> finalCollector) {
    this.finalCollector = finalCollector;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Supplier<Result<Collection<T>>> supplier() {
    return () -> Result.ok(new ArrayList<>());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public BiConsumer<Result<Collection<T>>, Result<T>> accumulator() {
    return (listResult, next) -> listResult
        .ifOkThen(list -> next
            .ifOkThen(list::add));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public BinaryOperator<Result<Collection<T>>> combiner() {
    return (firstResult, secondResult) -> firstResult
        .ifOkThen(first -> secondResult
            .ifOkThen(first::addAll));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Function<Result<Collection<T>>, Result<C>> finisher() {
    return listResult -> listResult
        .ifOkMap(Collection::stream)
        .ifOkMap(stream -> stream.collect(finalCollector));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Characteristics> characteristics() {
    return Collections.singleton(Characteristics.UNORDERED);
  }

  /**
   * Initialize a new collector instance.
   *
   * @param collector the collector to wrap.
   */
  public static <T, C> ResultCollector<T, C> aggregating(Collector<T, ?, C> collector) {
    return new ResultCollector<>(collector);
  }
}
