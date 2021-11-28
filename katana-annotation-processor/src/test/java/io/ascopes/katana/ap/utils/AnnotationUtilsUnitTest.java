package io.ascopes.katana.ap.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import org.assertj.core.api.BDDAssertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;

class AnnotationUtilsUnitTest {

  @Test
  void getAnnotationMirror_returns_the_first_annotation_mirror_when_matching() {
    // Given
    MockAnnotationMirrorHolder fooBarBaz = mockAnnotationMirrorType("FooBarBaz");
    MockAnnotationMirrorHolder doRayMe = mockAnnotationMirrorType("DoRayMe");
    MockAnnotationMirrorHolder trollLolLol = mockAnnotationMirrorType("TrollLolLol");

    Element annotatedElement = BDDMockito.mock(Element.class);
    BDDMockito.given(annotatedElement.getAnnotationMirrors())
        .willAnswer(ctx -> Arrays.asList(
            fooBarBaz.mirror,
            doRayMe.mirror,
            trollLolLol.mirror
        ));

    // When
    Result<? extends AnnotationMirror> result = AnnotationUtils
        .findAnnotationMirror(annotatedElement, trollLolLol.typeElement);

    // Then
    BDDAssertions.then(result.isOk())
        .isTrue();

    BDDAssertions.then(result.unwrap())
        .isSameAs(trollLolLol.mirror);
  }

  @Test
  void getAnnotationMirror_returns_ignored_if_not_found() {
    // Given
    MockAnnotationMirrorHolder fooBarBaz = mockAnnotationMirrorType("FooBarBaz");
    MockAnnotationMirrorHolder doRayMe = mockAnnotationMirrorType("DoRayMe");
    MockAnnotationMirrorHolder trollLolLol = mockAnnotationMirrorType("TrollLolLol");

    TypeElement notFoundElement = BDDMockito.mock(TypeElement.class);
    BDDMockito.given(notFoundElement.getSimpleName())
        .willReturn(new StubName("NotFoundElement"));

    Element annotatedElement = BDDMockito.mock(Element.class);
    BDDMockito.given(annotatedElement.getAnnotationMirrors())
        .willAnswer(ctx -> Arrays.asList(
            fooBarBaz.mirror,
            doRayMe.mirror,
            trollLolLol.mirror
        ));

    // When
    Result<? extends AnnotationMirror> result = AnnotationUtils
        .findAnnotationMirror(annotatedElement, notFoundElement);

    // Then
    BDDAssertions.then(result.isIgnored())
        .isTrue();
  }

  @Test
  void getValue_returns_the_match_when_found() {
    // Given
    AnnotationMirror mirror = BDDMockito.mock(AnnotationMirror.class);
    Map<ExecutableElement, AnnotationValue> elementValues = new HashMap<>();

    BDDMockito.given(mirror.getElementValues())
        .willAnswer(ctx -> elementValues);

    mockElementValuePair(elementValues, "foo", "abcdefg");
    mockElementValuePair(elementValues, "bar", 9182736);
    mockElementValuePair(elementValues, "baz", new boolean[]{false, true, false});

    // When
    Result<? extends AnnotationValue> result = AnnotationUtils.getValue(mirror, "baz");

    // Then
    BDDAssertions.then(result.isOk())
        .isTrue();

    BDDAssertions.then(result.unwrap().getValue())
        .isEqualTo(new boolean[]{false, true, false});
  }

  @Test
  void getValue_returns_not_ok_when_not_found() {
    // Given
    AnnotationMirror mirror = BDDMockito.mock(AnnotationMirror.class);
    Map<ExecutableElement, AnnotationValue> elementValues = new HashMap<>();

    BDDMockito.given(mirror.getElementValues())
        .willAnswer(ctx -> elementValues);

    mockElementValuePair(elementValues, "foo", "abcdefg");
    mockElementValuePair(elementValues, "bar", 9182736);
    mockElementValuePair(elementValues, "baz", new boolean[]{false, true, false});

    // When
    Result<? extends AnnotationValue> result = AnnotationUtils.getValue(mirror, "bork");

    // Then
    BDDAssertions.then(result.isOk())
        .isFalse();
  }

  static MockAnnotationMirrorHolder mockAnnotationMirrorType(String name) {
    AnnotationMirror mirror = BDDMockito.mock(AnnotationMirror.class);
    DeclaredType declaredType = BDDMockito.mock(DeclaredType.class);

    BDDMockito.given(mirror.getAnnotationType())
        .willReturn(declaredType);

    TypeElement typeElement = BDDMockito.mock(TypeElement.class);

    BDDMockito.given(declaredType.asElement())
        .willReturn(typeElement);

    Name stubName = new StubName(name);

    BDDMockito.given(typeElement.getSimpleName())
        .willReturn(stubName);

    MockAnnotationMirrorHolder holder = new MockAnnotationMirrorHolder();
    holder.mirror = mirror;
    holder.declaredType = declaredType;
    holder.typeElement = typeElement;
    holder.simpleName = stubName;
    return holder;
  }

  static void mockElementValuePair(
      Map<? super ExecutableElement, ? super AnnotationValue> elements,
      String name,
      Object value
  ) {
    ExecutableElement keyWrapper = BDDMockito.mock(ExecutableElement.class);
    AnnotationValue valueWrapper = BDDMockito.mock(AnnotationValue.class);

    BDDMockito.given(keyWrapper.getSimpleName())
        .willReturn(new StubName(name));
    BDDMockito.given(valueWrapper.getValue())
        .willReturn(value);

    elements.put(keyWrapper, valueWrapper);
  }

  static class MockAnnotationMirrorHolder {

    AnnotationMirror mirror;
    DeclaredType declaredType;
    TypeElement typeElement;
    Name simpleName;
  }

  /**
   * Stub for {@link Name} to use in tests.
   */
  static final class StubName implements Name {

    private final String content;

    public StubName(String content) {
      this.content = Objects.requireNonNull(content);
    }

    @Override
    public boolean contentEquals(CharSequence cs) {
      return this.content.contentEquals(cs);
    }

    @Override
    public int length() {
      return this.content.length();
    }

    @Override
    public char charAt(int index) {
      return this.content.charAt(index);
    }

    @Override
    @NonNull
    public CharSequence subSequence(int start, int end) {
      return this.content.subSequence(start, end);
    }

    @Override
    @NonNull
    public String toString() {
      return this.content;
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof Name && ((Name) other).contentEquals(this);
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.content);
    }
  }
}
