package io.ascopes.katana.ap.utils;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import org.checkerframework.common.util.report.qual.ReportInherit;

/**
 * A collector for results that aggregates the result state. A combiner is provided to combine a
 * stream of unwrapped OK results, and this is then wrapped in an OK result state if everything
 * succeeds. If a failure is detected, then collection goes no further, results are dropped, and an
 * error result is provided instead.
 *
 * <p>An example usage of this type is to provide the transformation from a Stream of {@code
 * Result<T>} as the input to a Result of {@code Collection<T>} as the output.
 *
 * @param <T> the type within the input results.
 * @param <C> the result of the input collector.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@ReportInherit
public final class ResultCollector<T, C>
    implements Collector<Result<T>, ResultCollector<T, C>.State, Result<C>> {

  private final Collector<T, ?, C> finalCollector;

  private ResultCollector(Collector<T, ?, C> finalCollector) {
    this.finalCollector = Objects.requireNonNull(finalCollector);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Supplier<ResultCollector<T, C>.State> supplier() {
    return State::new;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public BiConsumer<ResultCollector<T, C>.State, Result<T>> accumulator() {
    return (state, next) -> {
      if (state.failed.get()) {
        return;
      }
      if (next.isFailed()) {
        state.failed.set(true);
        return;
      }
      if (!next.isIgnored()) {
        state.builder.add(next.unwrap());
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public BinaryOperator<ResultCollector<T, C>.State> combiner() {
    return (firstState, secondState) -> {
      if (!firstState.failed.get()) {
        if (secondState.failed.get()) {
          firstState.failed.set(true);
        } else {
          secondState.builder.build().forEach(firstState.builder);
        }
      }
      return firstState;
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Function<ResultCollector<T, C>.State, Result<C>> finisher() {
    return state -> state.failed.get()
        ? Result.fail()
        : Result.ok(state.builder.build().collect(this.finalCollector));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Characteristics> characteristics() {
    return Collections.singleton(Characteristics.UNORDERED);
  }

  /**
   * Internal class for keeping track of the state while it is being aggregated.
   */
  public final class State {

    private final Stream.Builder<T> builder = Stream.builder();
    private final AtomicBoolean failed = new AtomicBoolean();

    private State() {
      // Internal detail only.
    }
  }

  /**
   * Aggregate a stream of results into a single result of whatever the given collector provides.
   *
   * @param collector the collector to aggregate with.
   * @param <T>       the input type.
   * @param <C>       the output type.
   * @return the collector.
   */
  public static <T, C> ResultCollector<T, C> aggregating(Collector<T, ?, C> collector) {
    return new ResultCollector<>(collector);
  }
}
