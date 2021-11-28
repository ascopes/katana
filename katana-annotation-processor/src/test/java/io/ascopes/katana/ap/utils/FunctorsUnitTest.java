package io.ascopes.katana.ap.utils;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FunctorsUnitTest {

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
  void flattenStream_flattens_the_stream() {
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

  @Test
  void flattenCollection_flattens_the_collections() {
    Stream<String> strings = Stream
        .of(
            Arrays.asList("sudo", "apt", "install"),
            Arrays.asList("yay", "-Syuu"),
            Arrays.asList("sdk", "install", "java")
        )
        .flatMap(Functors.flattenCollection());

    assertThat(strings)
        .containsExactly("sudo", "apt", "install", "yay", "-Syuu", "sdk", "install", "java");
  }
}
