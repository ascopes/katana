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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;

class ResultCollectorUnitTest {

  @Test
  void can_collect_successful_results_only_sequential() {
    Result<List<String>> results = Stream
        .of(
            Result.ok("foo"),
            Result.ok("bar"),
            Result.ok("baz")
        )
        .sequential()
        .collect(ResultCollector.aggregating(Collectors.toList()));

    BDDAssertions
        .assertThat(results.isOk())
        .isTrue();

    BDDAssertions
        .assertThat(results.unwrap())
        .containsExactly("foo", "bar", "baz");
  }

  @Test
  void can_not_collect_successful_results_if_failures_present_sequential() {
    Result<List<String>> results = Stream
        .of(
            Result.ok("foo"),
            Result.ok("bar"),
            Result.ok("baz"),
            Result.ok("bork"),
            Result.<String>fail("bang"),
            Result.ok("qux")
        )
        .sequential()
        .collect(ResultCollector.aggregating(Collectors.toList()));

    BDDAssertions
        .assertThat(results.isFailed())
        .isTrue();
  }


  @SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked", "rawtypes"})
  @Test
  void can_not_collect_successful_cleared_results_sequential() {
    BDDAssertions
        .thenCode(() -> Stream
            .<Result<Object>>of(
                Result.ok("foo"),
                Result.ok("bar"),
                Result.ok("baz"),
                (Result<Object>) (Result) Result.ok(),
                Result.ok("qux")
            )
            .sequential()
            .collect(ResultCollector.aggregating(Collectors.toList())))
        .isInstanceOf(IllegalStateException.class);
  }

  ////////////////////////
  //// Parallel tests ////
  ////////////////////////

  @Test
  void can_collect_successful_results_only_parallel() {
    Result<List<String>> results = Stream
        .of(
            Result.ok("foo"),
            Result.ok("bar"),
            Result.ok("baz")
        )
        .parallel()
        .collect(ResultCollector.aggregating(Collectors.toList()));

    BDDAssertions
        .assertThat(results.isOk())
        .isTrue();

    BDDAssertions
        .assertThat(results.unwrap())
        .containsExactly("foo", "bar", "baz");
  }

  @Test
  void can_not_collect_successful_and_results_if_failures_present_parallel() {
    Result<List<String>> results = Stream
        .of(
            Result.ok("foo"),
            Result.ok("bar"),
            Result.ok("baz"),
            Result.ok("bork"),
            Result.<String>fail("bang"),
            Result.ok("qux")
        )
        .parallel()
        .collect(ResultCollector.aggregating(Collectors.toList()));

    BDDAssertions
        .assertThat(results.isFailed())
        .isTrue();
  }


  @SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked", "rawtypes"})
  @Test
  void can_not_collect_successful_cleared_results_parallel() {
    BDDAssertions
        .thenCode(() -> Stream
            .<Result<Object>>of(
                Result.ok("foo"),
                Result.ok("bar"),
                Result.ok("baz"),
                (Result<Object>) (Result) Result.ok(),
                Result.ok("qux")
            )
            .parallel()
            .collect(ResultCollector.aggregating(Collectors.toList())))
        .isInstanceOf(IllegalStateException.class);
  }
}
