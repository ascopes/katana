package io.ascopes.katana.ap.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Base class for an iterator implementation that exposes the ability to coerce into a spliterator
 * or stream.
 *
 * @param <E> the iterator element type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
public interface StreamableIterator<E> extends Iterator<E> {

  @Override
  boolean hasNext();

  @Override
  E next() throws NoSuchElementException;

  int characteristics();

  @SuppressWarnings("MagicConstant")
  default Spliterator<E> spliterator() {
    return Spliterators.spliteratorUnknownSize(this, this.characteristics());
  }

  default Stream<E> stream() {
    return StreamSupport.stream(this.spliterator(), false);
  }

  static NoSuchElementException noMoreElementsException(String name) {
    return new NoSuchElementException("There are no more " + name + " to iterate over");
  }
}
