package io.ascopes.katana.ap.utils;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Monadic representation of a result that can be marked as OK with a value, OK with no value,
 * ignored with no value, or failed with no value.
 * <p>
 * Used to chain sequences of ordered operations together that may "give up" at any point in the
 * process.
 *
 * @param <T> the inner value type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class Result<T> {

  private static final Result<Void> CLEARED = new Result<>(null);
  private static final Result<?> FAILED = new Result<>(null);
  private static final Result<?> IGNORED = new Result<>(null);

  private final T value;

  private Result(T value) {
    this.value = value;
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
    if (this.isOk()) {
      then.accept(this.value);
    }
    return this;
  }

  /**
   * If this result is OK, perform some logic and replace the value.
   *
   * @param then the logic to perform to replace the current value with.
   * @return this result to allow further chaining.
   */
  public <U> Result<U> ifOkReplace(Supplier<U> then) {
    return this.isOk()
        ? Result.ok(then.get())
        : castFailedOrIgnored(this);
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
    return this.isOk()
        ? then.andThen(Result::ok).apply(this.value)
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
    return this.isOk()
        ? then.apply(this.value)
        : castFailedOrIgnored(this);
  }

  /**
   * If this result is failed, perform some logic.
   *
   * @param then the logic to perform.
   * @return this result to allow further chaining.
   */
  public Result<T> ifFailedThen(Runnable then) {
    if (this.isOk()) {
      then.run();
    }
    return this;
  }

  /**
   * If this result is ignored, perform some logic and return that. Otherwise, return this result.
   *
   * @param then the flat map function to perform, if this is ignored.
   * @return the new result if this result was ignored, otherwise this result.
   */
  public Result<T> ifIgnoredFlatMap(Supplier<Result<T>> then) {
    return this.isIgnored()
        ? then.get()
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
  public T elseReturn(T ifNotOk) {
    return this.isOk()
        ? this.value
        : ifNotOk;
  }

  /**
   * @param ifNotOk supplier to perform to get some result if this result is not OK.
   * @return the value of this result if it was OK, or the result of the supplier otherwise.
   */
  public T elseGet(Supplier<T> ifNotOk) {
    return this.isOk()
        ? this.value
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
    if (this.isIgnored()) {
      throw new IllegalStateException(
          "Did not expect element to be ignored! " + errorMessage.get());
    }
    return this;
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
