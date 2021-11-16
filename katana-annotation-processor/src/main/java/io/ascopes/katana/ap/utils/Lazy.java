package io.ascopes.katana.ap.utils;

import java.util.function.Supplier;

/**
 * Thread-safe lazy-evaluating attribute.
 *
 * @param <T> the attribute type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class Lazy<T> {

  private final Supplier<T> accessor;
  private final Object lock;
  private volatile boolean set;
  private volatile T object;

  /**
   * @param accessor the accessor to invoke lazily.
   */
  public Lazy(Supplier<T> accessor) {
    this.accessor = accessor;
    this.lock = new Object();
    this.set = false;
    this.object = null;
  }

  /**
   * Access the value of the attribute. If it is not yet known, evaluate it in a thread-safe way. If
   * it is known, just return it.
   *
   * @return the evaluated value.
   */
  public T get() {
    if (!this.set) {
      synchronized (this.lock) {
        if (!this.set) {
          this.object = this.accessor.get();
          this.set = true;
        }
      }
    }

    return this.object;
  }
}
