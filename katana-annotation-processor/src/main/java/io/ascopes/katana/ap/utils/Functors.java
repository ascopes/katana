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

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.checkerframework.common.util.report.qual.ReportCreation;
import org.checkerframework.common.util.report.qual.ReportInherit;

/**
 * Functional programming helpers.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@ReportCreation
@ReportInherit
public final class Functors {

  private Functors() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Flat map operation that applies to a stream of Optional objects and returns a stream of
   * non-optional objects. Any empty optionals get discarded.
   *
   * @param <T> the type within the optionals.
   * @return the function to apply.
   */
  public static <T> Function<Optional<T>, Stream<T>> removeEmpties() {
    return opt -> opt
        .map(Stream::of)
        .orElseGet(Stream::empty);
  }

  /**
   * Flat map operation that flattens a stream of streams into a single stream.
   *
   * @param <T> the elements in each stream.
   * @return the function to apply.
   */
  public static <T> Function<Stream<T>, Stream<T>> flattenStream() {
    // This is more explicit than just adding Function.identity and not immediately realising that
    // it is being used to flatten a stream of streams into a stream. JVM will likely JIT this out
    // anyway.
    return Function.identity();
  }

  /**
   * Flat map operation that flattens a stream of collections into a single stream.
   *
   * @param <C> the collection type.
   * @param <T> the elements in each collection.
   * @return the function to apply.
   */
  public static <C extends Collection<T>, T> Function<C, Stream<T>> flattenCollection() {
    // This is more explicit than just adding Collection.stream and not immediately realising that
    // it is being used to flatten a stream of collections into a stream. JVM will likely JIT this
    // out anyway.
    return Collection::stream;
  }
}
