package io.ascopes.katana.annotations;

import io.ascopes.katana.annotations.advices.CustomMethodAdvice;
import io.ascopes.katana.annotations.advices.CustomMethodAdvice.This;
import io.ascopes.katana.annotations.advices.CustomMethodAdvices;
import org.assertj.core.api.BDDAssertions;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
