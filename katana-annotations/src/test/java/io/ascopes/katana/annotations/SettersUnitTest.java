package io.ascopes.katana.annotations;

import org.junit.jupiter.api.Nested;

class SettersUnitTest {

  @Nested
  class SettersIncludeUnitTest extends CommonIncludeExcludeTestCases<Setters.Include> {

  }

  @Nested
  class SettersExcludeUnitTest extends CommonIncludeExcludeTestCases<Setters.Exclude> {

  }

  @Nested
  class SettersAdviceUnitTest extends CommonIncludeExcludeAdviceTestCases<Setters> {

  }

  @Nested
  class SettersFeatureUnitTest extends CommonAttributeFeatureTestCases<Setters> {

  }
}
