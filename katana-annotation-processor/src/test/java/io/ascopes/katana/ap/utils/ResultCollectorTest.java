package io.ascopes.katana.ap.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;

class ResultCollectorTest {

  @Test
  void can_collect_successful_results_only_sequential() {
    Result<List<String>> results = Stream
        .of(
            Result.ok("foo"),
            Result.ok("bar"),
            Result.ok("baz")
        )
        .sequential()
        .collect(ResultCollector.aggregating(Collectors.toList()));

    BDDAssertions
        .assertThat(results.isOk())
        .isTrue();

    BDDAssertions
        .assertThat(results.unwrap())
        .containsExactly("foo", "bar", "baz");
  }

  @Test
  void can_collect_ignored_results_only_sequential() {
    Result<List<String>> results = Stream
        .of(
            Result.<String>ignore(),
            Result.<String>ignore(),
            Result.<String>ignore()
        )
        .sequential()
        .collect(ResultCollector.aggregating(Collectors.toList()));

    BDDAssertions
        .assertThat(results.isOk())
        .isTrue();

    BDDAssertions
        .assertThat(results.unwrap())
        .isEmpty();
  }

  @Test
  void can_collect_successful_and_ignored_results_sequential() {
    Result<List<String>> results = Stream
        .of(
            Result.ok("foo"),
            Result.<String>ignore(),
            Result.ok("bar"),
            Result.<String>ignore(),
            Result.ok("baz"),
            Result.<String>ignore(),
            Result.<String>ignore(),
            Result.<String>ignore(),
            Result.ok("bork"),
            Result.<String>ignore(),
            Result.<String>ignore(),
            Result.ok("qux"),
            Result.<String>ignore()
        )
        .sequential()
        .collect(ResultCollector.aggregating(Collectors.toList()));

    BDDAssertions
        .assertThat(results.isOk())
        .isTrue();

    BDDAssertions
        .assertThat(results.unwrap())
        .containsExactly("foo", "bar", "baz", "bork", "qux");
  }

  @Test
  void can_not_collect_successful_and_ignored_results_if_failures_present_sequential() {
    Result<List<String>> results = Stream
        .of(
            Result.ok("foo"),
            Result.<String>ignore(),
            Result.ok("bar"),
            Result.<String>ignore(),
            Result.ok("baz"),
            Result.<String>ignore(),
            Result.<String>ignore(),
            Result.<String>ignore(),
            Result.ok("bork"),
            Result.<String>fail(),
            Result.<String>ignore(),
            Result.<String>ignore(),
            Result.ok("qux"),
            Result.<String>ignore()
        )
        .sequential()
        .collect(ResultCollector.aggregating(Collectors.toList()));

    BDDAssertions
        .assertThat(results.isFailed())
        .isTrue();
  }


  @SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked", "rawtypes"})
  @Test
  void can_not_collect_successful_cleared_results_sequential() {
    BDDAssertions
        .thenCode(() -> Stream
            .<Result<Object>>of(
                Result.ok("foo"),
                Result.ignore(),
                Result.ok("bar"),
                Result.ignore(),
                Result.ok("baz"),
                Result.ignore(),
                Result.ignore(),
                Result.ignore(),
                (Result<Object>) (Result) Result.ok(),
                Result.ignore(),
                Result.ignore(),
                Result.ok("qux"),
                Result.ignore()
            )
            .sequential()
            .collect(ResultCollector.aggregating(Collectors.toList())))
        .isInstanceOf(IllegalStateException.class);
  }

  ////////////////////////
  //// Parallel tests ////
  ////////////////////////

  @Test
  void can_collect_successful_results_only_parallel() {
    Result<List<String>> results = Stream
        .of(
            Result.ok("foo"),
            Result.ok("bar"),
            Result.ok("baz")
        )
        .parallel()
        .collect(ResultCollector.aggregating(Collectors.toList()));

    BDDAssertions
        .assertThat(results.isOk())
        .isTrue();

    BDDAssertions
        .assertThat(results.unwrap())
        .containsExactly("foo", "bar", "baz");
  }

  @Test
  void can_collect_ignored_results_only_parallel() {
    Result<List<String>> results = Stream
        .of(
            Result.<String>ignore(),
            Result.<String>ignore(),
            Result.<String>ignore()
        )
        .parallel()
        .collect(ResultCollector.aggregating(Collectors.toList()));

    BDDAssertions
        .assertThat(results.isOk())
        .isTrue();

    BDDAssertions
        .assertThat(results.unwrap())
        .isEmpty();
  }

  @Test
  void can_collect_successful_and_ignored_results_parallel() {
    Result<List<String>> results = Stream
        .of(
            Result.ok("foo"),
            Result.<String>ignore(),
            Result.ok("bar"),
            Result.<String>ignore(),
            Result.ok("baz"),
            Result.<String>ignore(),
            Result.<String>ignore(),
            Result.<String>ignore(),
            Result.ok("bork"),
            Result.<String>ignore(),
            Result.<String>ignore(),
            Result.ok("qux"),
            Result.<String>ignore()
        )
        .parallel()
        .collect(ResultCollector.aggregating(Collectors.toList()));

    BDDAssertions
        .assertThat(results.isOk())
        .isTrue();

    BDDAssertions
        .assertThat(results.unwrap())
        .containsExactly("foo", "bar", "baz", "bork", "qux");
  }

  @Test
  void can_not_collect_successful_and_ignored_results_if_failures_present_parallel() {
    Result<List<String>> results = Stream
        .of(
            Result.ok("foo"),
            Result.<String>ignore(),
            Result.ok("bar"),
            Result.<String>ignore(),
            Result.ok("baz"),
            Result.<String>ignore(),
            Result.<String>ignore(),
            Result.<String>ignore(),
            Result.ok("bork"),
            Result.<String>fail(),
            Result.<String>ignore(),
            Result.<String>ignore(),
            Result.ok("qux"),
            Result.<String>ignore()
        )
        .parallel()
        .collect(ResultCollector.aggregating(Collectors.toList()));

    BDDAssertions
        .assertThat(results.isFailed())
        .isTrue();
  }


  @SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked", "rawtypes"})
  @Test
  void can_not_collect_successful_cleared_results_parallel() {
    BDDAssertions
        .thenCode(() -> Stream
            .<Result<Object>>of(
                Result.ok("foo"),
                Result.ignore(),
                Result.ok("bar"),
                Result.ignore(),
                Result.ok("baz"),
                Result.ignore(),
                Result.ignore(),
                Result.ignore(),
                (Result<Object>) (Result) Result.ok(),
                Result.ignore(),
                Result.ignore(),
                Result.ok("qux"),
                Result.ignore()
            )
            .parallel()
            .collect(ResultCollector.aggregating(Collectors.toList())))
        .isInstanceOf(IllegalStateException.class);
  }
}
