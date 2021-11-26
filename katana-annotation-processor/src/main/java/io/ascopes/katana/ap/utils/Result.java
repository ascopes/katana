package io.ascopes.katana.ap.utils;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.PolyNull;

/**
 * Monadic representation of a result that can be marked as OK with a value, OK with no value,
 * ignored with no value, or failed with no value.
 * <p>
 * Used to chain sequences of ordered operations together that may "give up" at any point in the
 * process.
 * <p>
 * Due to the complex nature of this class, please keep methods documented.
 *
 * @param <T> the inner value type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class Result<T> {

  private static final Result<Void> CLEARED = new Result<>(null);
  private static final Result<?> FAILED = new Result<>(null);
  private static final Result<?> IGNORED = new Result<>(null);

  @PolyNull
  private final T value;

  private Result(@PolyNull T value) {
    this.value = value;
  }

  /**
   * Procedurally unwrap the result value.
   *
   * @return the result value.
   * @throws IllegalStateException if the result was not OK.
   */
  public T unwrap() throws IllegalStateException {
    this.assertNotCleared();
    if (this.isNotOk()) {
      throw new IllegalStateException("Cannot unwrap an ignored/failed result!");
    }

    return Objects.requireNonNull(this.value);
  }

  /**
   * @return true if the result is OK.
   */
  public boolean isOk() {
    return !this.isFailed() && !this.isIgnored();
  }

  /**
   * @return true if the result is not OK.
   */
  public boolean isNotOk() {
    return !this.isOk();
  }

  /**
   * @return true if the result is failed.
   */
  public boolean isFailed() {
    return this == FAILED;
  }

  /**
   * @return true if the result is ignored.
   */
  public boolean isIgnored() {
    return this == IGNORED;
  }

  /**
   * If this result is OK, perform some logic.
   *
   * @param then the logic to perform.
   * @return this result to allow further chaining.
   */
  public Result<T> ifOkThen(Consumer<T> then) {
    Objects.requireNonNull(then);
    this.assertNotCleared();
    if (this.isOk()) {
      then.accept(this.unwrap());
    }
    return this;
  }

  /**
   * If this result is OK, perform some logic and return that in a new OK result. Otherwise, return
   * this result.
   *
   * @param then the map function to perform, if OK.
   * @param <U>  the new result type.
   * @return the new result if this result was OK, otherwise this result.
   */
  public <U> Result<U> ifOkMap(Function<T, U> then) {
    Objects.requireNonNull(then);
    this.assertNotCleared();
    return this.isOk()
        ? then.andThen(Result::ok).apply(this.unwrap())
        : castFailedOrIgnored(this);
  }

  /**
   * If this result is OK, perform some logic and return that. Otherwise, return this result.
   *
   * @param then the flat map function to perform, if ok.
   * @param <U>  the new result type.
   * @return the new result if this result was ok, otherwise this result.
   */
  public <U> Result<U> ifOkFlatMap(Function<T, Result<U>> then) {
    Objects.requireNonNull(then);
    this.assertNotCleared();
    return this.isOk()
        ? Objects.requireNonNull(then.apply(this.unwrap()))
        : castFailedOrIgnored(this);
  }

  /**
   * If this result is OK, replace this result with the given result. Otherwise, return this
   * result.
   *
   * @param then the supplier of the result to replace with if this result is OK.
   * @return the new result.
   */
  public <U> Result<U> ifOkReplace(Supplier<Result<U>> then) {
    Objects.requireNonNull(then);
    return this.isOk()
        ? Objects.requireNonNull(then.get())
        : castFailedOrIgnored(this);
  }

  /**
   * Run some logic if the result is ignored.
   *
   * @param toRun logic to run if ignored.
   * @return this result.
   */
  // TODO(ascopes): unit tests
  public Result<T> ifIgnoredThen(Runnable toRun) {
    if (this.isIgnored()) {
      toRun.run();
    }
    return this;
  }

  /**
   * If the result is ignored, replace it with an OK result holding the given value.
   *
   * @param then the result value to replace with.
   * @return the result.
   */
  // TODO(ascopes): unit tests
  public Result<T> ifIgnoredReplace(T then) {
    return this.ifIgnoredReplace(() -> Result.ok(then));
  }

  /**
   * If this result is ignored, perform some logic and return that. Otherwise, return this result.
   *
   * @param then the flat map function to perform, if this is ignored.
   * @return the new result if this result was ignored, otherwise this result.
   */
  public Result<T> ifIgnoredReplace(Supplier<Result<T>> then) {
    Objects.requireNonNull(then);
    return this.isIgnored()
        ? Objects.requireNonNull(then.get())
        : this;
  }

  /**
   * @return a cleared result value that has no meaning other than the status. This will still
   * remain ok, ignored, or failed, but you will not be able to access the result.
   */
  public Result<Void> thenDiscardValue() {
    return this.isOk()
        ? CLEARED
        : castFailedOrIgnored(this);
  }

  /**
   * @param ifNotOk value to use if not OK.
   * @return the value of this result if it was OK, or the result of the supplier otherwise.
   */
  public @PolyNull T elseReturn(@PolyNull T ifNotOk) {
    this.assertNotCleared();
    return this.isOk()
        ? this.unwrap()
        : ifNotOk;
  }

  /**
   * @param ifNotOk supplier to perform to get some result if this result is not OK.
   * @return the value of this result if it was OK, or the result of the supplier otherwise.
   */
  public @PolyNull T elseGet(Supplier<@PolyNull T> ifNotOk) {
    this.assertNotCleared();
    Objects.requireNonNull(ifNotOk);
    return this.isOk()
        ? this.unwrap()
        : ifNotOk.get();
  }

  /**
   * Throw an exception if this result is ignored.
   *
   * @param errorMessage supplier of an additional error message to provide, if ignored.
   * @return this result if not ignored, to allow further call chaining.
   * @throws IllegalStateException if ignored.
   */
  public Result<T> assertNotIgnored(Supplier<String> errorMessage) throws IllegalStateException {
    Objects.requireNonNull(errorMessage);
    if (this.isIgnored()) {
      throw new IllegalStateException(
          "Did not expect element to be ignored! " + errorMessage.get());
    }
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Result<?>)) {
      return false;
    }

    Result<?> that = (Result<?>) other;

    return this == that || this.isOk() && that.isOk() && this.value == that.value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    if (this == CLEARED) {
      return Integer.MIN_VALUE + 1;
    }

    if (this == FAILED) {
      return Integer.MIN_VALUE + 2;
    }

    if (this == IGNORED) {
      return Integer.MIN_VALUE + 3;
    }

    return Objects.hash(this.value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    if (this == FAILED) {
      return "Result{failed}";
    }
    if (this == IGNORED) {
      return "Result{ignored}";
    }
    if (this == CLEARED) {
      return "Result{ok}";
    }
    return "Result{ok, " + this.value + "}";
  }

  private void assertNotCleared() {
    if (this == CLEARED) {
      throw new IllegalStateException("Cannot unwrap an empty OK result!");
    }
  }

  /**
   * @return an OK result that has no value.
   */
  public static Result<Void> ok() {
    return CLEARED;
  }

  /**
   * @return an OK result that has a value.
   */
  public static <T> Result<T> ok(T value) {
    return new Result<>(Objects.requireNonNull(value));
  }

  /**
   * @return a failed result with no value.
   */
  public static <T> Result<T> fail() {
    return castFailedOrIgnored(FAILED);
  }

  /**
   * @return an ignored result with no value.
   */
  public static <T> Result<T> ignore() {
    return castFailedOrIgnored(IGNORED);
  }

  @SuppressWarnings("unchecked")
  private static <T> Result<T> castFailedOrIgnored(Result<?> result) {
    assert result.isNotOk();
    // Type erasure is a beautiful thing sometimes.
    return (Result<T>) result;
  }
}
