package io.ascopes.katana.ap.utils;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;
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

  @PolyNull
  private final StackTraceGroup location;

  private Result() {
    this.value = null;
    this.reason = null;
    this.location = null;
  }

  private Result(T value) {
    this.value = value;
    this.reason = null;
    this.location = null;
  }

  private Result(String reason, StackTraceGroup location) {
    this.value = null;
    this.reason = reason;
    this.location = location;
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
   * Get the error's message, if this is an error.
   *
   * @return an optional of the error's message reason.
   */
  public Optional<String> getErrorReason() {
    return Optional.ofNullable(this.reason);
  }

  /**
   * Get the error's location, if this is an error.
   *
   * @return an optional of the error's location.
   */
  public Optional<StackTraceGroup> getErrorLocation() {
    return Optional.ofNullable(this.location);
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
        return castFailed(next);
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
        : castFailed(this);
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
        : castFailed(this);
  }

  /**
   * If the result is OK, then drop the value.
   *
   * <p>Acts as a cast to {@code Result<Void>} from any other generic result type.
   *
   * @return the result, emptied if OK. If failed, the result is just cast.
   */
  public Result<Void> ifOkDiscard() {
    // TODO(ascopes): unit test
    return this.isOk()
        ? ok()
        : castFailed(this);
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
        + "causes=" + Objects.requireNonNull(this.location).causes.length + 1
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
    return new Result<>(reason, stackTraceGroup(null));
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
    return new Result<>(failedResult.reason, stackTraceGroup(failedResult.location));
  }

  private static StackTraceGroup stackTraceGroup(@Nullable StackTraceGroup previousGroup) {
    // 0: .getStackTrace
    // 1: .stackTraceGroup
    // 2: .fail
    // 3: location of failure
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    return new StackTraceGroup(3, stackTrace, previousGroup);
  }

  @SuppressWarnings("unchecked")
  private static <T> Result<T> castFailed(Result<?> result) {
    assert result.isNotOk();
    // Type erasure is a beautiful thing sometimes.
    return (Result<T>) result;
  }

  /**
   * Descriptor of an erroneous result location, similar to that of an exception but without
   * actually throwing anything.
   */
  public static final class StackTraceGroup {

    private final StackTraceElement[] currentFrames;
    private final StackTraceElement[][] causes;

    private StackTraceGroup(
        int offset,
        StackTraceElement[] currentFrames,
        @Nullable StackTraceGroup cause
    ) {
      this.currentFrames = new StackTraceElement[currentFrames.length - offset];
      System.arraycopy(currentFrames, offset, this.currentFrames, 0, this.currentFrames.length);

      if (cause == null) {
        this.causes = new StackTraceElement[0][];
      } else {
        this.causes = new StackTraceElement[cause.causes.length + 1][];
        this.causes[0] = cause.currentFrames;
        System.arraycopy(cause.causes, 0, this.causes, 1, this.causes.length);
      }
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder("Triggered by ");
      appendTrace(builder, this.currentFrames);
      for (StackTraceElement[] cause : this.causes) {
        if (cause.length > 0) {
          builder.append("Caused by ");
          appendTrace(builder, cause);
        }
      }
      return builder.toString();
    }

    private static void appendTrace(StringBuilder builder, StackTraceElement[] frames) {
      builder.append("Result.fail() in ")
          .append(frames[0].getClassName())
          .append("#")
          .append(frames[0].getMethodName())
          .append("\n");

      for (StackTraceElement frame : frames) {
        builder.append("     at ").append(frame).append("\n");
      }
    }
  }
}
