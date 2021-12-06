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
