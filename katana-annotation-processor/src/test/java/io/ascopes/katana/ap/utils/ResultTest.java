package io.ascopes.katana.ap.utils;

import io.ascopes.katana.ap.mocking.GenericMocker;
import io.ascopes.katana.ap.mocking.GenericMocker.Ref;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.assertj.core.api.BDDAssertions;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.BDDMockito;

class ResultTest {

  @Test
  void can_unwrap_ok_result() {
    BDDAssertions.thenCode(Result.ok("foobar")::unwrap)
        .doesNotThrowAnyException();

    BDDAssertions
        .then(Result.ok("hello, world").unwrap())
        .isEqualTo("hello, world");
  }

  @Test
  void can_not_unwrap_empty_ok_result() {
    BDDAssertions
        .thenCode(Result.ok()::unwrap)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot unwrap an empty OK result!");
  }

  @ParameterizedTest
  @MethodSource("nonOk")
  void can_not_unwrap_ignored_result(Result<?> result) {
    BDDAssertions
        .thenCode(result::unwrap)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot unwrap an ignored/failed result!");
  }

  @Test
  void isOk_true_with_value() {
    BDDAssertions
        .then(Result.ok("foo").isOk())
        .isTrue();
  }

  @Test
  void isOk_true_without_value() {
    BDDAssertions
        .then(Result.ok().isOk())
        .isTrue();
  }

  @Test
  void isOk_false_when_ignored() {
    BDDAssertions
        .then(Result.ignore().isOk())
        .isFalse();
  }

  @Test
  void isOk_false_when_failed() {
    BDDAssertions
        .then(Result.fail().isOk())
        .isFalse();
  }


  @Test
  void isNotOk_false_when_ok_with_value() {
    BDDAssertions
        .then(Result.ok("foo").isNotOk())
        .isFalse();
  }

  @Test
  void isNotOk_false_when_ok_without_value() {
    BDDAssertions
        .then(Result.ok().isNotOk())
        .isFalse();
  }

  @Test
  void isNotOk_true_when_ignored() {
    BDDAssertions
        .then(Result.ignore().isNotOk())
        .isTrue();
  }

  @Test
  void isNotOk_true_when_failed() {
    BDDAssertions
        .then(Result.fail().isNotOk())
        .isTrue();
  }

  @Test
  void isIgnored_false_with_ok_value() {
    BDDAssertions
        .then(Result.ok("foo").isIgnored())
        .isFalse();
  }

  @Test
  void isIgnored_false_with_empty_ok() {
    BDDAssertions
        .then(Result.ok().isIgnored())
        .isFalse();
  }

  @Test
  void isIgnored_true_when_ignored() {
    BDDAssertions
        .then(Result.ignore().isIgnored())
        .isTrue();
  }

  @Test
  void isIgnored_false_when_failed() {
    BDDAssertions
        .then(Result.fail().isIgnored())
        .isFalse();
  }

  @Test
  void isFailed_false_with_ok_value() {
    BDDAssertions
        .then(Result.ok("foo").isFailed())
        .isFalse();
  }

  @Test
  void isFailed_false_with_empty_ok() {
    BDDAssertions
        .then(Result.ok().isFailed())
        .isFalse();
  }

  @Test
  void isFailed_false_when_ignored() {
    BDDAssertions
        .then(Result.ignore().isFailed())
        .isFalse();
  }

  @Test
  void isFailed_true_when_failed() {
    BDDAssertions
        .then(Result.fail().isFailed())
        .isTrue();
  }

  @Test
  void ifOkThen_when_ok_with_value() {
    Consumer<String> consumer = GenericMocker.mock(new Ref<Consumer<String>>() {
    });

    Result.ok("123").ifOkThen(consumer);

    BDDMockito
        .then(consumer)
        .should()
        .accept("123");
  }

  @Test
  void ifOkThen_when_ok_not_valued_will_fail() {
    Consumer<Void> consumer = GenericMocker.mock(new Ref<Consumer<Void>>() {
    });

    BDDAssertions
        .thenCode(() -> Result.ok().ifOkThen(consumer))
        .isInstanceOf(IllegalStateException.class);

    BDDMockito
        .then(consumer)
        .shouldHaveNoInteractions();
  }

  @ParameterizedTest
  @MethodSource("nonOk")
  void ifOkThen_when_not_ok(Result<Object> result) {
    Consumer<Object> consumer = GenericMocker.mock(new Ref<Consumer<Object>>() {
    });

    BDDAssertions
        .thenCode(() -> result.ifOkThen(consumer))
        .doesNotThrowAnyException();

    BDDMockito
        .then(consumer)
        .shouldHaveNoInteractions();
  }

  @Test
  void ifOkMap_when_ok_with_value() {
    // Cannot use references here as mockito will not mock them properly.
    @SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"})
    Function<String, Integer> fn = BDDMockito.spy(new Function<String, Integer>() {
      @Override
      public Integer apply(String s) {
        return Integer.parseInt(s);
      }
    });

    BDDAssertions
        .then(Result.ok("91827").ifOkMap(fn).unwrap())
        .isEqualTo(91827);

    BDDMockito
        .then(fn)
        .should()
        .apply("91827");
  }

  @Test
  void ifOkMap_when_ok_with_no_value() {
    // Cannot use references here as mockito will not mock them properly.
    @SuppressWarnings({"Convert2Lambda"})
    Function<Void, Integer> fn = BDDMockito.spy(new Function<Void, Integer>() {
      @Override
      public Integer apply(Void v) {
        return BDDAssertions.fail("unreachable code");
      }
    });

    BDDAssertions
        .thenCode(() -> Result.ok().ifOkMap(fn))
        .isInstanceOf(IllegalStateException.class);

    BDDMockito
        .then(fn)
        .shouldHaveNoInteractions();
  }

  @ParameterizedTest
  @MethodSource("nonOk")
  void ifOkMap_when_not_ok(Result<Object> result) {
    // Cannot use references here as mockito will not mock them properly.
    @SuppressWarnings({"Convert2Lambda"})
    Function<Object, Integer> fn = BDDMockito.spy(new Function<Object, Integer>() {
      @Override
      public Integer apply(Object v) {
        return BDDAssertions.fail("unreachable code");
      }
    });

    BDDAssertions
        .then(result.ifOkMap(fn).isNotOk())
        .isTrue();

    BDDMockito
        .then(fn)
        .shouldHaveNoInteractions();
  }

  @ParameterizedTest
  @MethodSource("replacements")
  void ifOkFlatMap_when_ok_with_value(Result<Object> replacement) {
    // Cannot use references here as mockito will not mock them properly.
    @SuppressWarnings({"Convert2Lambda"})
    Function<String, Result<Object>> fn = BDDMockito.spy(new Function<String, Result<Object>>() {
      @Override
      public Result<Object> apply(String s) {
        return replacement;
      }
    });

    BDDAssertions
        .then(Result.ok("131415").ifOkFlatMap(fn))
        .isSameAs(replacement);

    BDDMockito
        .then(fn)
        .should()
        .apply("131415");
  }

  @Test
  void ifOkFlatMap_when_ok_with_no_value() {
    // Cannot use references here as mockito will not mock them properly.
    @SuppressWarnings({"Convert2Lambda"})
    Function<Void, Result<Integer>> fn = BDDMockito.spy(new Function<Void, Result<Integer>>() {
      @Override
      public Result<Integer> apply(Void v) {
        return BDDAssertions.fail("unreachable code");
      }
    });

    BDDAssertions
        .thenCode(() -> Result.ok().ifOkFlatMap(fn))
        .isInstanceOf(IllegalStateException.class);

    BDDMockito
        .then(fn)
        .shouldHaveNoInteractions();
  }

  @ParameterizedTest
  @MethodSource("nonOk")
  void ifOkFlatMap_when_not_ok(Result<Object> result) {
    // Cannot use references here as mockito will not mock them properly.
    @SuppressWarnings({"Convert2Lambda"})
    Function<Object, Result<Integer>> fn = BDDMockito.spy(new Function<Object, Result<Integer>>() {
      @Override
      public Result<Integer> apply(Object v) {
        return BDDAssertions.fail("unreachable code");
      }
    });

    BDDAssertions
        .then(result.ifOkFlatMap(fn).isNotOk())
        .isTrue();

    BDDMockito
        .then(fn)
        .shouldHaveNoInteractions();
  }

  @ParameterizedTest
  @MethodSource("replacements")
  void ifOkReplace_when_ok_with_value(Result<Object> replacement) {
    Result<Object> initial = Result.ok(new Object());
    BDDAssertions
        .then(initial.ifOkReplace(() -> replacement))
        .isSameAs(replacement);
  }

  @ParameterizedTest
  @MethodSource("replacements")
  void ifOkReplace_when_ok_without_value(Result<Object> replacement) {
    Result<Void> initial = Result.ok();
    BDDAssertions
        .then(initial.ifOkReplace(() -> replacement))
        .isSameAs(replacement);
  }

  @ParameterizedTest
  @MethodSource("replacements")
  void ifOkReplace_when_failed(Result<Object> replacement) {
    Result<Void> initial = Result.fail();
    BDDAssertions
        .then(initial.ifOkReplace(() -> replacement))
        .isSameAs(initial);
  }

  @ParameterizedTest
  @MethodSource("replacements")
  void ifOkReplace_when_ignored(Result<Object> replacement) {
    Result<Void> initial = Result.ignore();
    BDDAssertions
        .then(initial.ifOkReplace(() -> replacement))
        .isSameAs(initial);
  }

  @ParameterizedTest
  @MethodSource("replacements")
  void ifIgnoredReplace_when_ok_with_value(Result<Object> replacement) {
    Result<Object> initial = Result.ok(new Object());
    BDDAssertions
        .then(initial.ifIgnoredReplace(() -> replacement))
        .isSameAs(initial);
  }

  @ParameterizedTest
  @MethodSource("replacements")
  void ifIgnoredReplace_when_ok_without_value(Result<Void> replacement) {
    Result<Void> initial = Result.ok();
    BDDAssertions
        .then(initial.ifIgnoredReplace(() -> replacement))
        .isSameAs(initial);
  }

  @ParameterizedTest
  @MethodSource("replacements")
  void ifIgnoredReplace_when_failed(Result<Object> replacement) {
    Result<Object> initial = Result.fail();
    BDDAssertions
        .then(initial.ifIgnoredReplace(() -> replacement))
        .isSameAs(initial);
  }

  @ParameterizedTest
  @MethodSource("replacements")
  void ifIgnoredReplace_when_ignored(Result<Object> replacement) {
    Result<Object> initial = Result.ignore();
    BDDAssertions
        .then(initial.ifIgnoredReplace(() -> replacement))
        .isSameAs(replacement);
  }

  @Test
  void thenDiscardValue_when_ok_with_value() {
    Result<Object> initial = Result.ok(UUID.randomUUID().toString());

    BDDAssertions
        .then(initial.thenDiscardValue())
        .isNotSameAs(initial)
        .matches(Result::isOk);

    BDDAssertions
        .thenCode(() -> initial.thenDiscardValue().unwrap())
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void thenDiscardValue_when_ok_without() {
    Result<Void> initial = Result.ok();
    BDDAssertions
        .then(initial.thenDiscardValue())
        .isSameAs(initial);
  }

  @Test
  void thenDiscardValue_when_failed() {
    Result<Void> initial = Result.fail();
    BDDAssertions
        .then(initial.thenDiscardValue())
        .isSameAs(initial);
  }

  @Test
  void thenDiscardValue_when_ignored() {
    Result<Void> initial = Result.ignore();
    BDDAssertions
        .then(initial.thenDiscardValue())
        .isSameAs(initial);
  }

  @Test
  void elseReturn_when_ok_with_value() {
    String initialValue = UUID.randomUUID().toString();
    Result<Object> initial = Result.ok(initialValue);
    BDDAssertions
        .then(initial.elseReturn(new Object()))
        .isEqualTo(initialValue);
  }

  @Test
  void elseReturn_when_ok_without_value() {
    Result<Void> initial = Result.ok();
    BDDAssertions
        .thenCode(() -> initial.elseReturn(null))
        .isInstanceOf(IllegalStateException.class);
  }

  @ParameterizedTest
  @MethodSource("randomNullableContent")
  void elseReturn_when_ignored(String content) {
    Result<Object> initial = Result.ignore();
    BDDAssertions
        .then(initial.elseReturn(content))
        .isEqualTo(content);
  }

  @ParameterizedTest
  @MethodSource("randomNullableContent")
  void elseReturn_when_failed(String content) {
    Result<Object> initial = Result.fail();
    BDDAssertions
        .then(initial.elseReturn(content))
        .isEqualTo(content);
  }


  @Test
  void elseGet_when_ok_with_value() {
    String initialValue = UUID.randomUUID().toString();
    Result<Object> initial = Result.ok(initialValue);
    BDDAssertions
        .then(initial.elseGet(Object::new))
        .isEqualTo(initialValue);
  }

  @Test
  void elseGet_when_ok_without_value() {
    Result<Void> initial = Result.ok();
    BDDAssertions
        .thenCode(() -> initial.elseGet(() -> null))
        .isInstanceOf(IllegalStateException.class);
  }

  @ParameterizedTest
  @MethodSource("randomNullableContent")
  void elseGet_when_ignored(String content) {
    Result<Object> initial = Result.ignore();
    BDDAssertions
        .then(initial.elseGet(() -> content))
        .isEqualTo(content);
  }

  @ParameterizedTest
  @MethodSource("randomNullableContent")
  void elseGet_when_failed(String content) {
    Result<Object> initial = Result.fail();
    BDDAssertions
        .then(initial.elseGet(() -> content))
        .isEqualTo(content);
  }

  @Test
  void assertNotIgnored_when_ok_with_value() {
    Result<Object> initial = Result.ok(new Object());

    BDDAssertions
        .thenCode(() -> initial.assertNotIgnored(() -> "woof!"))
        .doesNotThrowAnyException();
  }

  @Test
  void assertNotIgnored_when_ok_without_value() {
    Result<Void> initial = Result.ok();

    BDDAssertions
        .thenCode(() -> initial.assertNotIgnored(() -> "woof!"))
        .doesNotThrowAnyException();
  }

  @Test
  void assertNotIgnored_when_failed() {
    Result<Object> initial = Result.fail();

    BDDAssertions
        .thenCode(() -> initial.assertNotIgnored(() -> "woof!"))
        .doesNotThrowAnyException();
  }

  @Test
  void assertNotIgnored_when_ignored() {
    Result<Object> initial = Result.ignore();

    String message = UUID.randomUUID().toString();

    BDDAssertions
        .thenCode(() -> initial.assertNotIgnored(() -> message))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Did not expect element to be ignored! " + message);
  }

  @ParameterizedTest(name = "{0}.equals({1}) == {2}")
  @MethodSource("equalityChecks")
  void equals_checks(Result<?> first, Object second, boolean isEqual) {
    BDDAssertions
        .assertThat(first.equals(second))
        .isEqualTo(isEqual);
  }

  @ParameterizedTest(name = "{0}.hashCode() == {1}.hashCode() == {2}")
  @MethodSource("hashChecks")
  void hashChecks(Result<?> first, Object second, boolean isEqual) {
    BDDAssertions
        .assertThat(first.hashCode() == second.hashCode())
        .withFailMessage(
            "hashcode equality was %s, first hash is %s, second hash is %s",
            !isEqual,
            first.hashCode(),
            second.hashCode()
        )
        .isEqualTo(isEqual);
  }

  static <T> Stream<Result<T>> nonOk() {
    return Stream.of(Result.fail(), Result.ignore());
  }

  static Stream<Result<?>> replacements() {
    return Stream.of(
        Result.ok(UUID.randomUUID().toString()),
        Result.ok(),
        Result.ignore(),
        Result.fail()
    );
  }

  static Stream<@Nullable String> randomNullableContent() {
    return Stream.of(
        UUID.randomUUID().toString(),
        "  ",
        "",
        null
    );
  }

  static Stream<Arguments> equalityChecks() {
    Stream.Builder<Arguments> args = Stream.builder();
    String randomString = UUID.randomUUID().toString();

    List<Supplier<Result<?>>> cases = Arrays.asList(
        () -> Result.ok(randomString),
        Result::ok,
        Result::fail,
        Result::ignore
    );

    for (int i = 0; i < cases.size(); ++i) {
      Result<?> first = cases.get(i).get();
      args.add(Arguments.of(first, null, false))
          .add(Arguments.of(first, randomString, false))
          .add(Arguments.of(first, new Object(), false));
      for (int j = 0; j < cases.size(); ++j) {
        boolean isEqual = i == j;
        Result<?> second = cases.get(j).get();
        args.add(Arguments.of(first, second, isEqual));
      }
    }

    return args.build();
  }

  static Stream<Arguments> hashChecks() {
    Stream.Builder<Arguments> args = Stream.builder();
    List<Supplier<Result<?>>> cases = Arrays.asList(
        () -> Result.ok(12),
        Result::ok,
        Result::fail,
        Result::ignore
    );

    for (int i = 0; i < cases.size(); ++i) {
      Result<?> first = cases.get(i).get();
      for (int j = 0; j < cases.size(); ++j) {
        boolean isEqual = i == j;
        Result<?> second = cases.get(j).get();
        args.add(Arguments.of(first, second, isEqual));
      }
    }

    return args.build();
  }
}
