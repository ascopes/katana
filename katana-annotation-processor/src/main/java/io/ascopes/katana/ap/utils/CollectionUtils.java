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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
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

  private CollectionUtils() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Collector that produces a sorted set.
   *
   * @param comparator the comparator to use in the set.
   * @param <T>        the element type.
   * @return the collector.
   */
  public static <T> Collector<T, ?, SortedSet<T>> toSortedSet(Comparator<T> comparator) {
    // TODO(ascopes): unit test this
    return Collectors.toCollection(() -> new TreeSet<>(comparator));
  }

  /**
   * Collector that produces a sorted set.
   *
   * @param comparableKey a function that returns a key to compare the elements against.
   * @param <T>           the element type.
   * @param <U>           the comparable key for the elements.
   * @return the collector.
   */
  public static <T, U extends Comparable<U>> Collector<T, ?, SortedSet<T>> toSortedSet(
      Function<T, U> comparableKey
  ) {
    // TODO(ascopes): unit test this
    return toSortedSet(Comparator.comparing(comparableKey));
  }


  /**
   * Shallow-freeze a list.
   *
   * @param list the list to freeze.
   * @param <T>  the list type.
   * @return the list, wrapped in a delegating type that makes it unmodifiable.
   */
  public static <T> List<T> freezeList(List<T> list) {
    return Collections.unmodifiableList(Objects.requireNonNull(list));
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
   * Shallow-freeze a map.
   *
   * @param map the map to freeze.
   * @param <K> the map's key type.
   * @param <V> the map's value type.
   * @return the map, wrapped in a delegating type that makes it unmodifiable.
   */
  public static <K, V> Map<K, V> freezeMap(Map<K, V> map) {
    return Collections.unmodifiableMap(Objects.requireNonNull(map));
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
