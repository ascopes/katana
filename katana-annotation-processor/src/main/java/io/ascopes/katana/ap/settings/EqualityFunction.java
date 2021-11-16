package io.ascopes.katana.ap.settings;

import java.util.function.BiPredicate;

/**
 * A function for determining type-safe equality of two values.
 *
 * @param <T> the value type.
 */
@FunctionalInterface
public interface EqualityFunction<T> extends BiPredicate<T, T> {
  // Type alias
}
