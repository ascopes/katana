package io.ascopes.katana.ap.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class FunctorsTest {

  @Test
  void removeEmpties_removes_empty_optionals() {
    Stream<String> strings = Stream
        .of(
            Optional.of("foo"),
            Optional.of("bar"),
            Optional.<String>empty(),
            Optional.of("baz"),
            Optional.<String>empty(),
            Optional.of("bork")
        )
        .flatMap(Functors.removeEmpties());

    assertThat(strings)
        .containsExactly("foo", "bar", "baz", "bork");
  }

  @Test
  void flatten_flattens_the_stream() {
    Stream<String> strings = Stream
        .of(
            Stream.of("foo", "bar", "baz"),
            Stream.of("doh", "ray", "me"),
            Stream.of("troll", "lol", "lol")
        )
        .flatMap(Functors.flattenStream());

    assertThat(strings)
        .containsExactly("foo", "bar", "baz", "doh", "ray", "me", "troll", "lol", "lol");
  }
}
