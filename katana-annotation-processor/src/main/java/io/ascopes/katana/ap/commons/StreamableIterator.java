package io.ascopes.katana.ap.commons;

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
  /**
   * @return the next element.
   * @throws NoSuchElementException if no more elements are available to yield.
   */
  @Override
  E next() throws NoSuchElementException;

  /**
   * @return true if more elements can be yielded, or false if the iterator is terminated.
   */
  @Override
  boolean hasNext();

  /**
   * @return this iterator wrapped within a spliterator.
   */
  @SuppressWarnings("MagicConstant")
  default Spliterator<E> spliterator() {
    return Spliterators.spliteratorUnknownSize(this, this.characteristics());
  }

  /**
   * @return this iterator wrapped within a stream.
   */
  default Stream<E> stream() {
    return StreamSupport.stream(this.spliterator(), false);
  }

  /**
   * @return the spliterator characteristics to allow.
   */
  int characteristics();
}
