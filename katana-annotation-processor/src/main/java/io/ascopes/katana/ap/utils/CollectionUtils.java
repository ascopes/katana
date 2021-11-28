package io.ascopes.katana.ap.utils;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import org.checkerframework.common.util.report.qual.ReportCreation;
import org.checkerframework.common.util.report.qual.ReportInherit;

/**
 * Collection helper methods.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@ReportCreation
@ReportInherit
public final class CollectionUtils {
  // TODO(ascopes): unit tests!

  private CollectionUtils() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Shallow-freeze a set.
   *
   * @param set the set to freeze.
   * @param <T> the set type.
   * @return the set, wrapped in a delegating type that makes it unmodifiable.
   */
  public static <T> Set<T> freezeSet(Set<T> set) {
    return Collections.unmodifiableSet(Objects.requireNonNull(set));
  }

  /**
   * Shallow-freeze a sorted set.
   *
   * @param set the sorted set to freeze.
   * @param <T> the sorted set type.
   * @return the sorted set, wrapped in a delegating type that makes it unmodifiable.
   */
  public static <T> SortedSet<T> freezeSortedSet(SortedSet<T> set) {
    return Collections.unmodifiableSortedSet(Objects.requireNonNull(set));
  }

  /**
   * Shallow-freeze a sorted map.
   *
   * @param map the sorted map to freeze.
   * @param <K> the sorted map's key type.
   * @param <V> the sorted map's value type.
   * @return the sorted map, wrapped in a delegating type that makes it unmodifiable.
   */
  public static <K, V> SortedMap<K, V> freezeSortedMap(SortedMap<K, V> map) {
    return Collections.unmodifiableSortedMap(Objects.requireNonNull(map));
  }

  /**
   * Deep-freeze a sorted map of sets.
   *
   * @param map the sorted map to freeze.
   * @param <K> the sorted map's key type.
   * @param <V> the element type for each value set in the sorted map.
   * @return a copy of the map, where each set has been shallow-frozen and the map itself is shallow
   *     frozen too.
   */
  public static <K, V> SortedMap<K, Set<V>> freezeSortedMapOfSets(SortedMap<K, Set<V>> map) {
    Objects.requireNonNull(map);
    TreeMap<K, Set<V>> copy = new TreeMap<>(map);
    copy.replaceAll((k, set) -> freezeSet(set));
    return freezeSortedMap(copy);
  }
}
