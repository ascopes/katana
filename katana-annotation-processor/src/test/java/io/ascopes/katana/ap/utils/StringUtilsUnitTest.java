/*
 * Copyright (C) 2021 Ashley Scopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    BDDAssertions.then(StringUtils.prependAOrAn(input))
        .isEqualTo(expectedOutput);
  }
}
