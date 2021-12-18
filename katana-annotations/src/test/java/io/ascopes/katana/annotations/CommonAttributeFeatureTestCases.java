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

package io.ascopes.katana.annotations;

import io.ascopes.katana.annotations.internal.AttributeFeature;
import io.ascopes.katana.annotations.internal.CustomizableAttributeFeature;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

@SuppressWarnings("unused")
abstract class CommonAttributeFeatureTestCases<T extends Enum<T>>
    extends TypeAware<T> {

  private final Class<T> attributeFeatureType;

  CommonAttributeFeatureTestCases() {
    this.attributeFeatureType = this.getGenericType();
  }

  @Test
  void INCLUDE_ALL_has_isIncludeAll_as_true() {
    BDDAssertions
        .assertThat(this.getEnumConstantNamed("INCLUDE_ALL"))
        .isPresent()
        .get()
        .matches(AttributeFeature::isIncludeAll)
        .matches(not(AttributeFeature::isExcludeAll))
        .matches(not(AttributeFeature::isDisabled));
  }

  @Test
  void EXCLUDE_ALL_has_isExcludeAll_as_true() {
    BDDAssertions
        .assertThat(this.getEnumConstantNamed("EXCLUDE_ALL"))
        .isPresent()
        .get()
        .matches(AttributeFeature::isExcludeAll)
        .matches(not(AttributeFeature::isIncludeAll))
        .matches(not(AttributeFeature::isDisabled));
  }

  @TestFactory
  Stream<DynamicTest> DISABLED_has_isDisabled_as_true_if_DISABLED_defined() {
    Optional<AttributeFeature> disabled = this.getEnumConstantNamed("DISABLED");

    if (!disabled.isPresent()) {
      return Stream.empty();
    }

    return Stream.of(DynamicTest.dynamicTest(
        "DISABLED_has_isDisabled_as_true",
        () -> BDDAssertions
            .assertThat(disabled)
            .get()
            .matches(AttributeFeature::isDisabled)
            .matches(not(AttributeFeature::isIncludeAll))
            .matches(not(AttributeFeature::isExcludeAll)))
    );
  }

  @TestFactory
  Stream<DynamicTest> CUSTOM_has_isCustom_as_true_if_instanceof_CustomizableAttributeFeature() {
    if (!CustomizableAttributeFeature.class.isAssignableFrom(this.attributeFeatureType)) {
      return Stream.empty();
    }

    Optional<CustomizableAttributeFeature> custom = this
        .getEnumConstantNamed("CUSTOM")
        .map(CustomizableAttributeFeature.class::cast);

    return Stream.of(DynamicTest.dynamicTest(
        "CUSTOM_has_isCustom_as_true",
        () -> BDDAssertions
            .assertThat(custom)
            .get()
            .matches(CustomizableAttributeFeature::isCustom)
            .matches(not(AttributeFeature::isIncludeAll))
            .matches(not(AttributeFeature::isExcludeAll))
            .matches(not(AttributeFeature::isDisabled))
    ));
  }

  protected Optional<AttributeFeature> getEnumConstantNamed(String name) {
    for (T enumMember : this.attributeFeatureType.getEnumConstants()) {
      if (enumMember.name().equals(name)) {
        return Optional
            .of(enumMember)
            .map(AttributeFeature.class::cast);
      }
    }

    return Optional.empty();
  }

  protected static <T> Predicate<T> not(Predicate<T> predicate) {
    return predicate.negate();
  }
}
