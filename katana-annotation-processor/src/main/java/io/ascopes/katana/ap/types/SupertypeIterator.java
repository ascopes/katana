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

package io.ascopes.katana.ap.types;

import io.ascopes.katana.ap.utils.KatanaIterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Spliterator;
import java.util.TreeSet;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Streamable iterator which yields the given type element and all unique supertypes, in a top-down
 * orientation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class SupertypeIterator extends KatanaIterator<TypeElement> {

  private final Types typeUtils;
  private final Queue<TypeElement> upcoming;
  private final TreeSet<TypeElement> seen;

  /**
   * Initialize this iterator.
   *
   * @param typeUtils the type utilities to use for introspection.
   * @param root      the type root to start at.
   */
  public SupertypeIterator(Types typeUtils, TypeElement root) {
    this.typeUtils = typeUtils;
    this.upcoming = new LinkedList<>();
    this.upcoming.offer(root);
    this.seen = new TreeSet<>(this::compareTypeElements);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasNext() {
    return !this.upcoming.isEmpty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TypeElement next() throws NoSuchElementException {
    @Nullable
    TypeElement next = this.upcoming.poll();

    if (next == null) {
      throw noMoreElementsException("superinterfaces");
    }

    if (!(next.getSuperclass() instanceof NoType)) {
      this.processSupertype(next.getSuperclass());
    }

    for (TypeMirror interfaceTypeMirror : next.getInterfaces()) {
      this.processSupertype(interfaceTypeMirror);
    }

    return next;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int characteristics() {
    return Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.ORDERED;
  }

  private void processSupertype(TypeMirror typeMirror) {
    if (typeMirror == null) {
      return;
    }

    TypeElement element = (TypeElement) this.typeUtils.asElement(typeMirror);
    if (!this.seen.contains(element)) {
      this.upcoming.add(element);
      this.seen.add(element);
    }
  }

  private int compareTypeElements(TypeElement first, TypeElement second) {
    return first.getQualifiedName().toString().compareTo(second.getQualifiedName().toString());
  }
}
