package io.ascopes.katana.ap.introspection;

import io.ascopes.katana.ap.commons.StreamableIterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Spliterator;
import java.util.TreeSet;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

/**
 * Streamable iterator which yields the given type element and all unique super interfaces, in a
 * top-down orientation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class SuperInterfaceIterator implements StreamableIterator<TypeElement> {
  private final Types typeUtils;
  private final Queue<TypeElement> upcoming;
  private final TreeSet<TypeElement> seen;

  /**
   * @param typeUtils utilities for type introspection.
   * @param root the root type element to start at.
   */
  public SuperInterfaceIterator(Types typeUtils, TypeElement root) {
    this.typeUtils = typeUtils;
    this.upcoming = new LinkedList<>();
    this.upcoming.offer(root);
    this.seen = new TreeSet<>(this::compareTypeElements);
  }

  @Override
  public TypeElement next() throws NoSuchElementException {
    TypeElement next = this.upcoming.poll();

    if (next == null) {
      throw new NoSuchElementException("No more elements to yield");
    }

    for (TypeMirror mirror : next.getInterfaces()) {
      TypeElement element = (TypeElement) this.typeUtils.asElement(mirror);
      if (!this.seen.contains(element)) {
        this.upcoming.add(element);
        this.seen.add(element);
      }
    }

    return next;
  }

  @Override
  public boolean hasNext() {
    return !this.upcoming.isEmpty();
  }

  @Override
  public int characteristics() {
    return Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.NONNULL;
  }

  private int compareTypeElements(TypeElement first, TypeElement second) {
    return first.getQualifiedName().toString().compareTo(second.getQualifiedName().toString());
  }
}
