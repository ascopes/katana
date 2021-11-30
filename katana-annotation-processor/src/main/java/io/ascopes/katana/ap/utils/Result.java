package io.ascopes.katana.ap.utils;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.common.util.report.qual.ReportInherit;

/**
 * Monadic representation of a result that can be marked as OK with a value, OK with no value,
 * ignored with no value, or failed with no value.
 *
 * <p>Used to chain sequences of ordered operations together that may "give up" at any point in the
 * process.
 *
 * @param <T> the inner value type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@ReportInherit
public final class Result<T> {
  @PolyNull
  private final T value;

  @PolyNull
  private final String reason;

  private final StackTraceElement from;

  private Result() {
    this.value = null;
    this.reason = null;
    this.from = null;
  }

  private Result(T value) {
    this.value = value;
    this.reason = null;
    this.from = null;
  }

  private Result(String reason, StackTraceElement from) {
    this.value = null;
    this.reason = reason;
    this.from = from;
  }

  /**
   * Procedurally unwrap the result value.
   *
   * @return the result value.
   * @throws IllegalStateException if the result was not OK.
   */
  public T unwrap() throws IllegalStateException {
    this.assertNotCleared();
    if (this.isFailed()) {
      throw new IllegalStateException(this.reason);
    }

    return Objects.requireNonNull(this.value);
  }

  /**
   * Determine if the result is OK.
   *
   * @return true if the result is OK.
   */
  public boolean isOk() {
    return !this.isFailed();
  }

  /**
   * Determine if the result is not OK.
   *
   * @return true if the result is not OK.
   */
  public boolean isNotOk() {
    return !this.isOk();
  }

  /**
   * Determine if the result is failed.
   *
   * @return true if the result is failed.
   */
  public boolean isFailed() {
    return this.reason != null;
  }

  /**
   * If this result is OK, perform some logic.
   *
   * @param then the logic to perform.
   * @return this result to allow further chaining.
   */
  public Result<T> ifOk(Runnable then) {
    // TODO(ascopes): unit test
    Objects.requireNonNull(then);
    if (this.isOk()) {
      then.run();
    }
    return this;
  }

  /**
   * If this result is OK, perform some logic.
   *
   * @param then the logic to perform.
   * @return this result to allow further chaining.
   */
  public Result<T> ifOk(Consumer<T> then) {
    Objects.requireNonNull(then);
    this.assertNotCleared();
    if (this.isOk()) {
      then.accept(this.unwrap());
    }
    return this;
  }

  /**
   * If this result is OK, then try to apply the value in this result to another function. If that
   * function returns an ignored or failed result, then that result will become this result. If that
   * result was OK, then this result is returned.
   *
   * <p>This is equivalent to {@link #ifOk(Consumer)}, except any failed/ignored result from
   * the next function is considered in the returned result here.
   *
   * @param then the logic to invoke if this was OK.
   * @return the result.
   */
  public Result<T> ifOkCheck(Function<T, Result<?>> then) {
    // TODO(ascopes): unit test
    Objects.requireNonNull(then);
    this.assertNotCleared();
    if (this.isOk()) {
      Result<?> next = then.apply(this.unwrap());
      if (!next.isOk()) {
        return castFailedOrIgnored(next);
      }
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
   * @param <U>  the new result type.
   * @return the new result.
   */
  public <U> Result<U> ifOkReplace(Supplier<Result<U>> then) {
    Objects.requireNonNull(then);
    return this.isOk()
        ? Objects.requireNonNull(then.get())
        : castFailedOrIgnored(this);
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
    return Objects.hash(this.value, this.reason);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    if (this.isOk()) {
      if (this.value == null) {
        return "Result{ok, empty}";
      }
      return "Result{ok, value=" + this.value + "}";
    }

    return "Result{"
        + "failed, "
        + "reason=" + StringUtils.quoted(this.reason) + ", "
        + "raisedAt=" + StringUtils.quoted(this.from)
        + "}";
  }

  private void assertNotCleared() {
    if (this.value == null && this.reason == null) {
      throw new IllegalStateException("Cannot unwrap an empty OK result!");
    }
  }

  /**
   * Generate an empty OK result.
   *
   * @return an OK result that has no value.
   */
  public static Result<Void> ok() {
    return new Result<>();
  }

  /**
   * Generate an OK result with some value.
   *
   * @return an OK result that has a value.
   */
  public static <T> Result<T> ok(T value) {
    return new Result<>(Objects.requireNonNull(value));
  }

  /**
   * Generate a failed result.
   *
   * @param reason the reason for failing.
   * @return a failed result with no value.
   */
  public static <T> Result<T> fail(String reason) {
    StackTraceElement frame = Thread.currentThread().getStackTrace()[2];
    return new Result<>(reason, frame);
  }

  /**
   * Generate a failed result from another failed result, taking the reason message.
   *
   * @param failedResult the failed result to copy.
   * @return a failed result with no value.
   */
  public static <T> Result<T> fail(Result<?> failedResult) {
    if (!failedResult.isFailed()) {
      throw new IllegalStateException("Cannot create a failed result from a non-failed result");
    }
    return new Result<>(failedResult.reason, failedResult.from);
  }

  @SuppressWarnings("unchecked")
  private static <T> Result<T> castFailedOrIgnored(Result<?> result) {
    assert result.isNotOk();
    // Type erasure is a beautiful thing sometimes.
    return (Result<T>) result;
  }
}
