package io.ascopes.katana.annotations;

import io.ascopes.katana.annotations.internal.CustomMethodAdvice;
import io.ascopes.katana.annotations.internal.CustomMethodAdvice.This;
import io.ascopes.katana.annotations.internal.CustomMethodAdvices;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

abstract class CommonCustomAdviceTestCases<T extends Enum<T>> extends TypeAware<T> {
  final Class<T> featureType;
  final Signature[] expectedSignatures;

  CommonCustomAdviceTestCases(Signature ... expectedSignatures) {
    this.featureType = this.getGenericType();
    this.expectedSignatures = expectedSignatures;
  }

  @Test
  void only_expected_advices_are_defined() {
    // Only matters for multiple annotations.
    if (this.featureType.isAnnotationPresent(CustomMethodAdvices.class)) {
      CustomMethodAdvices advices = this.featureType.getAnnotation(CustomMethodAdvices.class);
      BDDAssertions
          .assertThat(advices.value())
          .hasSize(this.expectedSignatures.length);

      BDDAssertions
          .assertThat(this.featureType.getAnnotation(CustomMethodAdvice.class))
          .withFailMessage("Unexpected %s annotation", CustomMethodAdvice.class.getCanonicalName())
          .isNull();
    } else {
      BDDAssertions
          .assertThat(this.featureType)
          .hasAnnotation(CustomMethodAdvice.class);
    }
  }

  @TestFactory
  Stream<DynamicTest> custom_advices_are_defined_correctly() {
    return Stream
        .of(this.expectedSignatures)
        .map(signature -> DynamicTest.dynamicTest("for method := " + signature, () -> {
          if (this.featureType.isAnnotationPresent(CustomMethodAdvice.class)) {
            CustomMethodAdvice advice = this.featureType.getAnnotation(CustomMethodAdvice.class);
            BDDAssertions
                .assertThat(new Signature(advice))
                .isEqualToComparingFieldByField(signature);
          } else if (this.featureType.isAnnotationPresent(CustomMethodAdvices.class)) {
            CustomMethodAdvices advices = this.featureType.getAnnotation(CustomMethodAdvices.class);
            List<String> seen = new ArrayList<>();
            for (CustomMethodAdvice advice : advices.value()) {
              boolean matchAnnotation = signature.annotation.equals(advice.annotation());
              boolean matchReturn = signature.returnType.equals(advice.returns());
              boolean matchParams = Arrays.equals(signature.parameterTypes, advice.consumes());
              if (matchAnnotation && matchReturn && matchParams) {
                return;
              }
              seen.add(new Signature(advice).toString());
            }

            BDDAssertions
                .fail(
                    "Failed to find a matching signature for %s. We only found %s",
                    signature,
                    seen
                );
          } else {
            BDDAssertions.fail(
                "No %s or %s annotation class on %s",
                CustomMethodAdvice.class.getCanonicalName(),
                CustomMethodAdvices.class.getCanonicalName(),
                this.featureType.getCanonicalName()
            );
          }
        }));
  }

  static class Signature {
    private final Class<? extends Annotation> annotation;
    private final Class<?> returnType;
    private final Class<?>[] parameterTypes;

    Signature(CustomMethodAdvice from) {
      this(from.annotation(), from.returns(), from.consumes());
    }

    Signature(
        Class<? extends Annotation> annotation,
        Class<?> returnType,
        Class<?>... parameterTypes
    ) {
      this.annotation = annotation;
      this.returnType = returnType;
      this.parameterTypes = parameterTypes;
    }

    @Override
    public String toString() {
      String params = Stream
          .of(this.parameterTypes)
          .map(this::toStringType)
          .collect(Collectors.joining(", "));
      return "(" + params + ") -> " + this.toStringType(this.returnType);
    }

    private String toStringType(Class<?> type) {
      if (type.equals(This.class)) {
        return "this";
      }
      return type.getCanonicalName();
    }
  }
}
