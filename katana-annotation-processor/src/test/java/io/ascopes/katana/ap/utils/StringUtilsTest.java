package io.ascopes.katana.ap.utils;

import com.github.jknack.handlebars.EscapingStrategy;
import com.github.jknack.handlebars.Handlebars;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StringUtilsTest {

  static Handlebars handlebars;

  @BeforeAll
  static void setUpAll() {
    handlebars = new Handlebars()
        .with(EscapingStrategy.NOOP)
        .registerHelpers(StringUtils.class);
  }

  @ParameterizedTest
  @CsvSource({
      "hello world!,\"hello world!\"",
      ",\"null\"",
      "can contain 'inner' quotes,\"can contain 'inner' quotes\"",
      "double quotes get \"totally\" escaped,\"double quotes get \\\"totally\\\" escaped\"",
      "backslashes also get \\escaped,\"backslashes also get \\\\escaped\"",
  })
  void can_quote_content(String input, String expectedOutput) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("input", input);
    String actualOutput = handlebars.compileInline("My message: {{ quoted input }}")
        .apply(params);

    BDDAssertions.then(actualOutput)
        .isEqualTo("My message: " + expectedOutput);
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
  void can_put_a_at_start_of_word(String input, String expectedOutput) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("variable", input);

    String actualOutput = handlebars.compileInline("I saw {{ a variable }}")
        .apply(params);

    BDDAssertions.then(actualOutput)
        .isEqualTo("I saw " + expectedOutput);
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
  void can_put_an_at_start_of_word(String input, String expectedOutput) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("variable", input);

    String actualOutput = handlebars.compileInline("I saw {{ an variable }}")
        .apply(params);

    BDDAssertions.then(actualOutput)
        .isEqualTo("I saw " + expectedOutput);
  }
}
