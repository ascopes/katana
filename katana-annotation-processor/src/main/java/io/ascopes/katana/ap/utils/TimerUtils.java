/*
 * Copyright (C) 2021 Ashley Scopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
