package io.ascopes.katana.ap.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class KatanaIteratorUnitTest {

  @Test
  void spliterator_returns_valid_spliterator() {
    KatanaIteratorImpl iterator = new KatanaIteratorImpl();
    Spliterator<String> spliterator = iterator.spliterator();

    assertThat(spliterator.tryAdvance(spliteratorExpect("foo")))
        .isTrue();

    assertThat(spliterator.tryAdvance(spliteratorExpect("bar")))
        .isTrue();

    assertThat(spliterator.tryAdvance(spliteratorExpect("baz")))
        .isTrue();

    assertThat(spliterator.tryAdvance(spliteratorDoNotExpect()))
        .isFalse();

    assertThat(spliterator.characteristics())
        .isEqualTo(iterator.characteristics());
  }

  @Test
  void stream_returns_valid_stream() {
    assertThat(new KatanaIteratorImpl().stream())
        .containsExactly("foo", "bar", "baz");
  }

  @Test
  void noMoreElementsException_returns_NoMoreElementsException() {
    assertThat(KatanaIterator.noMoreElementsException("lemons"))
        .hasMessage("There are no more lemons to iterate over");
  }

  static Consumer<String> spliteratorExpect(String expected) {
    return actual -> assertThat(actual).isEqualTo(expected);
  }

  static Consumer<String> spliteratorDoNotExpect() {
    return actual -> fail("Did not expect any element, but received '" + actual + "'");
  }

  static class KatanaIteratorImpl extends KatanaIterator<String> {

    private final Iterator<String> innerIterator = Arrays
        .asList("foo", "bar", "baz")
        .iterator();

    @Override
    public boolean hasNext() {
      return this.innerIterator.hasNext();
    }

    @Override
    public String next() throws NoSuchElementException {
      return this.innerIterator.next();
    }

    @Override
    public int characteristics() {
      return DISTINCT | ORDERED | NONNULL;
    }
  }
}
