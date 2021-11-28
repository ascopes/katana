package io.ascopes.katana.annotations;

import org.junit.jupiter.api.Nested;

class TransientUnitTest {

  @Nested
  class TransientIncludeUnitTest extends CommonIncludeExcludeTestCases<Transient.Include> {

  }

  @Nested
  class TransientExcludeUnitTest extends CommonIncludeExcludeTestCases<Transient.Exclude> {

  }

  @Nested
  class TransientAdviceUnitTest extends CommonIncludeExcludeAdviceTestCases<Transient> {

  }

  @Nested
  class TransientFeatureUnitTest extends CommonAttributeFeatureTestCases<Transient> {

  }
}
