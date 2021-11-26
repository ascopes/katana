package io.ascopes.katana.ap.utils;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 * Collection helper methods.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
// TODO(ascopes): unit tests!
public abstract class CollectionUtils {

  private CollectionUtils() {
    throw new UnsupportedOperationException("static-only class");
  }

  public static <T> Set<T> freeze(Set<T> set) {
    return Collections.unmodifiableSet(Objects.requireNonNull(set));
  }

  public static <T> SortedSet<T> freeze(SortedSet<T> set) {
    return Collections.unmodifiableSortedSet(Objects.requireNonNull(set));
  }

  public static <K, V> SortedMap<K, V> freeze(SortedMap<K, V> map) {
    return Collections.unmodifiableSortedMap(Objects.requireNonNull(map));
  }

  public static <K, V> SortedMap<K, Set<V>> deepFreeze(SortedMap<K, Set<V>> map) {
    Objects.requireNonNull(map);
    TreeMap<K, Set<V>> copy = new TreeMap<>(map);
    copy.replaceAll((k, set) -> freeze(set));
    return freeze(copy);
  }
}
