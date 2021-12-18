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

class EqualityUnitTest {

  @Nested
  class EqualityIncludeUnitTest extends CommonIncludeExcludeTestCases<Equality.Include> {

  }

  @Nested
  class EqualityExcludeUnitTest extends CommonIncludeExcludeTestCases<Equality.Exclude> {

  }

  @Nested
  class EqualityAdviceUnitTest extends CommonIncludeExcludeAdviceTestCases<Equality> {

  }

  @Nested
  class EqualityFeatureUnitTest extends CommonAttributeFeatureTestCases<Equality> {

  }

  @Nested
  class EqualityCustomMethodUnitTest extends CommonCustomAdviceTestCases<Equality> {
    EqualityCustomMethodUnitTest() {
      super(
          new Signature(Equality.CustomEquals.class, boolean.class, This.class, Object.class),
          new Signature(Equality.CustomHashCode.class, int.class, This.class)
      );
    }
  }
}
