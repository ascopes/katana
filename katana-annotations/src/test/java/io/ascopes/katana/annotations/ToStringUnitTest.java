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

import io.ascopes.katana.annotations.internal.CustomMethodAdvice.This;
import org.junit.jupiter.api.Nested;

class ToStringUnitTest {

  @Nested
  class ToStringIncludeUnitTest extends CommonIncludeExcludeTestCases<ToString.Include> {

  }

  @Nested
  class ToStringExcludeUnitTest extends CommonIncludeExcludeTestCases<ToString.Exclude> {

  }

  @Nested
  class ToStringAdviceUnitTest extends CommonIncludeExcludeAdviceTestCases<ToString> {

  }

  @Nested
  class ToStringFeatureUnitTest extends CommonAttributeFeatureTestCases<ToString> {

  }

  @Nested
  class ToStringCustomMethodUnitTest extends CommonCustomAdviceTestCases<ToString> {
    ToStringCustomMethodUnitTest() {
      super(new Signature(ToString.CustomToString.class, String.class, This.class));
    }
  }
}
