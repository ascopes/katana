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

import com.squareup.javapoet.ClassName;
import io.ascopes.katana.annotations.Visibility;
import java.util.Collections;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CodeGenUtilsUnitTest {

  @ParameterizedTest
  @MethodSource("modifiersCases")
  void modifiers_produces_correct_modifiers_from_visibility_flag(
      Visibility input,
      Modifier[] expectedOutput
  ) {
    BDDAssertions
        .then(CodeGenUtils.modifiers(input))
        .isEqualTo(expectedOutput);
  }

  @Test
  void override_produces_override_annotation() {
    BDDAssertions
        .then(CodeGenUtils.override())
        .hasFieldOrPropertyWithValue("type", ClassName.get(Override.class))
        .hasFieldOrPropertyWithValue("members", Collections.emptyMap());
  }

  static Stream<Arguments> modifiersCases() {
    return Stream.of(
        Arguments.of(Visibility.PRIVATE, new Modifier[]{Modifier.PRIVATE}),
        Arguments.of(Visibility.PROTECTED, new Modifier[]{Modifier.PROTECTED}),
        Arguments.of(Visibility.PUBLIC, new Modifier[]{Modifier.PUBLIC}),
        Arguments.of(Visibility.PACKAGE_PRIVATE, new Modifier[]{})
    );
  }
}
