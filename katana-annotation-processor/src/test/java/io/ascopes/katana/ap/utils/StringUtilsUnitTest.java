package io.ascopes.katana.ap.utils;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StringUtilsUnitTest {

  @ParameterizedTest
  @CsvSource({
      "hello world!,\"hello world!\"",
      ",\"null\"",
      "can contain 'inner' quotes,\"can contain 'inner' quotes\"",
      "double quotes get \"totally\" escaped,\"double quotes get \\\"totally\\\" escaped\"",
      "backslashes also get \\escaped,\"backslashes also get \\\\escaped\"",
  })
  void can_quote_content(String input, String expectedOutput) {
    BDDAssertions.then(StringUtils.quoted(input))
        .isEqualTo(expectedOutput);
  }

  @ParameterizedTest
  @CsvSource({
      "animal,an animal",
      "bat,a bat",
      "cat,a cat",
      "dog,a dog",
      "egg,an egg",
      "fox,a fox",
      "goat,a goat",
      "hippo,a hippo",
      "igloo,an igloo",
      "jaguar,a jaguar",
      "kitkat,a kitkat",
      "lemur,a lemur",
      "mouse,a mouse",
      "octopus,an octopus",
      "pet,a pet",
      "quota,a quota",
      "rag,a rag",
      "sock,a sock",
      "tree,a tree",
      "underpass,an underpass",
      "vertex,a vertex",
      "winch,a winch",
      "xylophone,a xylophone",
      "yack,a yack",
      "zebra,a zebra",
  })
  void can_put_a_at_start_of_word(String input, String expectedOutput) {
    BDDAssertions.then(StringUtils.a(input))
        .isEqualTo(expectedOutput);
  }
}
