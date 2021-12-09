package io.ascopes.katana.ap.utils;

/**
 * A utility class to help time the execution of logic.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class TimerUtils {

  private TimerUtils() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Measure the time taken to perform a task.
   *
   * @param runnable the task to perform.
   * @param <E>      the exception type that could be thrown.
   * @return the time taken to perform the task.
   */
  public static <E extends Throwable> double timing(ThrowingRunnable<E> runnable) throws E {
    long start = System.nanoTime();
    runnable.run();
    return (System.nanoTime() - start) / 1_000_000.0;
  }

  /**
   * Runnable that can throw any exception.
   *
   * @param <E> the exception type to throw.
   */
  public interface ThrowingRunnable<E extends Throwable> {

    /**
     * Execute the logic.
     *
     * @throws E the exception that can be thrown.
     */
    void run() throws E;
  }
}
