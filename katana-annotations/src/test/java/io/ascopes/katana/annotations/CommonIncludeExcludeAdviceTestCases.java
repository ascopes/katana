package io.ascopes.katana.annotations;

import io.ascopes.katana.annotations.internal.AttributeFeature;
import io.ascopes.katana.annotations.internal.ExclusionAdvice;
import io.ascopes.katana.annotations.internal.InclusionAdvice;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

abstract class CommonIncludeExcludeAdviceTestCases<T extends Enum<T> & AttributeFeature>
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
        .assertThat(inclusion.value())
        .isNotEqualTo(exclusion.value());
  }
}
