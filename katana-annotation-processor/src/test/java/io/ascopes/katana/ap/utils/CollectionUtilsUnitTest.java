package io.ascopes.katana.ap.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CollectionUtilsUnitTest {

  @Test
  void freezeList_fails_if_null() {
    BDDAssertions
        .thenCode(() -> CollectionUtils.freezeList(null))
        .isInstanceOf(NullPointerException.class);
  }

  @ParameterizedTest
  @MethodSource("mutableOperationsCollection")
  void freezeList_produces_frozen_list(Consumer<List<Object>> operation) {
    List<Object> list = CollectionUtils.freezeList(new ArrayList<>());
    BDDAssertions
        .thenCode(() -> operation.accept(list))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void freezeSet_fails_if_null() {
    BDDAssertions
        .thenCode(() -> CollectionUtils.freezeSet(null))
        .isInstanceOf(NullPointerException.class);
  }

  @ParameterizedTest
  @MethodSource("mutableOperationsCollection")
  void freezeSet_produces_frozen_set(Consumer<Set<Object>> operation) {
    Set<Object> set = CollectionUtils.freezeSet(new HashSet<>());
    BDDAssertions
        .thenCode(() -> operation.accept(set))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void freezeSortedSet_fails_if_null() {
    BDDAssertions
        .thenCode(() -> CollectionUtils.freezeSortedSet(null))
        .isInstanceOf(NullPointerException.class);
  }

  @ParameterizedTest
  @MethodSource("mutableOperationsCollection")
  void freezeSortedSet_produces_frozen_sorted_set(Consumer<SortedSet<Object>> operation) {
    SortedSet<Object> set = CollectionUtils.freezeSortedSet(new TreeSet<>());
    BDDAssertions
        .thenCode(() -> operation.accept(set))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void freezeSortedMap_fails_if_null() {
    BDDAssertions
        .thenCode(() -> CollectionUtils.freezeSortedMap(null))
        .isInstanceOf(NullPointerException.class);
  }

  @ParameterizedTest
  @MethodSource("mutableOperationsMap")
  void freezeSortedMap_produces_frozen_sorted_map(Consumer<SortedMap<Object, Object>> operation) {
    SortedMap<Object, Object> map = CollectionUtils.freezeSortedMap(new TreeMap<>());
    BDDAssertions
        .thenCode(() -> operation.accept(map))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void freezeSortedMapOfSets_fails_if_null() {
    BDDAssertions
        .thenCode(() -> CollectionUtils.freezeSortedMapOfSets(null))
        .isInstanceOf(NullPointerException.class);
  }

  @ParameterizedTest
  @MethodSource("mutableOperationsMap")
  void freezeSortedMapOfSets_produces_frozen_sorted_map(
      Consumer<SortedMap<Object, Set<Object>>> operation
  ) {
    SortedMap<Object, Set<Object>> map = CollectionUtils.freezeSortedMapOfSets(new TreeMap<>());
    BDDAssertions
        .thenCode(() -> operation.accept(map))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @ParameterizedTest
  @MethodSource("mutableOperationsCollection")
  void freezeSortedMapOfSets_result_contains_frozen_sets(Consumer<Set<Object>> operation) {
    HashSet<Object> firstSet = new HashSet<>();
    firstSet.add(new Object());
    firstSet.add(new Object());

    TreeMap<Object, Set<Object>> map = new TreeMap<>();
    map.put("firstSet", firstSet);

    SortedMap<Object, Set<Object>> frozenMap = CollectionUtils.freezeSortedMapOfSets(map);

    BDDAssertions
        .thenCode(() -> operation.accept(frozenMap.get("firstSet")))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  static <T extends Collection<Object>> Stream<Consumer<T>> mutableOperationsCollection() {
    return Stream
        .of(
            named("add", c -> c.add(new Object())),
            named("addAll", c -> c.addAll(Arrays.asList(new Object(), new Object()))),
            named("remove", c -> c.remove(new Object())),
            named("removeAll", c -> c.removeAll(Collections.singletonList(1234))),
            named("retainAll", c -> c.retainAll(Collections.singletonList(1234)))
        );
  }

  static <T extends Map<Object, Object>> Stream<Consumer<T>> mutableOperationsMap() {
    return Stream
        .of(
            named("put", m -> m.put(new Object(), new Object())),
            named("putIfAbsent", m -> m.putIfAbsent(new Object(), new Object())),
            named("putAll", m -> m.putAll(new HashMap<>())),
            named("compute", m -> m.compute(new Object(), (k, v) -> new Object())),
            named("computeIfAbsent", m -> m.computeIfAbsent(new Object(), k -> new Object())),
            named("computeIfPresent", m -> m.computeIfPresent(1020, (k, v) -> "foobar")),
            named("replace/2", m -> m.replace("key", "value")),
            named("replace/3", m -> m.replace("key", "oldValue", "newValue")),
            named("replaceAll", m -> m.replaceAll((a, b) -> "bang")),
            named("remove/1", m -> m.remove(2.718281828459045)),
            named("remove/2", m -> m.remove(2.718281828459045, "foo"))
        );
  }

  static <T> Consumer<T> named(String name, Consumer<T> consumer) {
    return new Consumer<T>() {
      @Override
      public void accept(T arg) {
        consumer.accept(arg);
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }
}
