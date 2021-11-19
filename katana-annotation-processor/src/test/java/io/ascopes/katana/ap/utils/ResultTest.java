package io.ascopes.katana.ap.utils;

import java.util.stream.Stream;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ResultTest {

  @Test
  void can_unwrap_ok_result() {
    BDDAssertions.thenCode(Result.ok("foobar")::unwrap)
        .doesNotThrowAnyException();

    BDDAssertions
        .then(Result.ok("hello, world").unwrap())
        .isEqualTo("hello, world");
  }

  @Test
  void can_not_unwrap_empty_ok_result() {
    BDDAssertions
        .thenCode(Result.ok()::unwrap)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot unwrap an empty OK result!");
  }

  @ParameterizedTest
  @MethodSource("nonOk")
  void can_not_unwrap_ignored_result(Result<?> result) {
    BDDAssertions
        .thenCode(result::unwrap)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot unwrap an ignored/failed result!");
  }

  static <T> Stream<Result<T>> nonOk() {
    return Stream.of(Result.fail(), Result.ignore());
  }
}
