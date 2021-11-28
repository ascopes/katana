package io.ascopes.katana.annotations;

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
}
