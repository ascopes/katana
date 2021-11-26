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
public final class ResultCollector<T, C>
    implements Collector<Result<T>, ResultCollector<T, C>.State, Result<C>> {

  private final Collector<T, ?, C> finalCollector;

  private ResultCollector(Collector<T, ?, C> finalCollector) {
    this.finalCollector = Objects.requireNonNull(finalCollector);
  }

  @Override
  public Supplier<ResultCollector<T, C>.State> supplier() {
    return State::new;
  }

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

  @Override
  public Function<ResultCollector<T, C>.State, Result<C>> finisher() {
    return state -> state.failed.get()
        ? Result.fail()
        : Result.ok(state.builder.build().collect(this.finalCollector));
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.singleton(Characteristics.UNORDERED);
  }

  public static <T, C> ResultCollector<T, C> aggregating(Collector<T, ?, C> collector) {
    return new ResultCollector<>(collector);
  }

  public final class State {

    private final Stream.Builder<T> builder = Stream.builder();
    private final AtomicBoolean failed = new AtomicBoolean();

    private State() {
      // Internal detail only.
    }
  }
}
