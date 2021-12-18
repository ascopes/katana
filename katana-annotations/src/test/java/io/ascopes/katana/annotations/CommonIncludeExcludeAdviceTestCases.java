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

import io.ascopes.katana.annotations.internal.ExclusionAdvice;
import io.ascopes.katana.annotations.internal.InclusionAdvice;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

abstract class CommonIncludeExcludeAdviceTestCases<T extends Enum<T>>
    extends TypeAware<T> {

  private final Class<T> attributeFeatureType;

  CommonIncludeExcludeAdviceTestCases() {
    this.attributeFeatureType = this.getGenericType();
  }

  @Order(1)
  @Test
  void has_InclusionAdvice_annotation() {
    BDDAssertions
        .assertThat(this.attributeFeatureType)
        .hasAnnotation(InclusionAdvice.class);
  }

  @Order(2)
  @Test
  void has_ExclusionAdvice_annotation() {
    BDDAssertions
        .assertThat(this.attributeFeatureType)
        .hasAnnotation(ExclusionAdvice.class);
  }

  @Order(3)
  @Test
  void InclusionAdvice_differs_from_ExclusionAdvice() {
    InclusionAdvice inclusion = this.attributeFeatureType.getAnnotation(InclusionAdvice.class);
    ExclusionAdvice exclusion = this.attributeFeatureType.getAnnotation(ExclusionAdvice.class);

    BDDAssertions
        .assertThat(inclusion.annotation())
        .isNotEqualTo(exclusion.annotation());
  }
}
