package io.ascopes.katana.ap.iterators;

import java.util.NoSuchElementException;
import java.util.Spliterator;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Iterator across all parent packages for a given element.
 *
 * <p>This follows a bottom-up approach, and will thus include the empty unnamed package last.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class PackageIterator extends KatanaIterator<PackageElement> {

  private final Elements elementUtils;

  @Nullable
  private PackageElement next;

  /**
   * Initialize this iterator.
   *
   * @param elementUtils the element utilities to use for introspection.
   * @param start        the first element to start from.
   */
  public PackageIterator(Elements elementUtils, Element start) {
    this.elementUtils = elementUtils;
    this.next = elementUtils.getPackageOf(start);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasNext() {
    return this.next != null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PackageElement next() throws NoSuchElementException {
    PackageElement current = this.next;

    if (current == null) {
      throw KatanaIterator.noMoreElementsException("parent packages");
    }

    if (current.isUnnamed()) {
      this.next = null;
      return current;
    }

    String name = current.getQualifiedName().toString();
    int lastPeriod = name.lastIndexOf('.');

    // <= to prevent index out of bounds on dodgy packages that start with '.', as I am not sure
    // if all compilers guard against that.
    if (lastPeriod <= 0) {
      // Return the unnamed root package.
      this.next = this.elementUtils.getPackageElement("");
    } else {
      name = name.substring(0, lastPeriod);
      this.next = this.elementUtils.getPackageElement(name);
    }

    return current;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int characteristics() {
    return Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.ORDERED;
  }
}
