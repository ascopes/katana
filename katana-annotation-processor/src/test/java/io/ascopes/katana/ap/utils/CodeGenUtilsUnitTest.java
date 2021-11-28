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
