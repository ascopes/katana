package io.ascopes.katana.annotations;

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
}
