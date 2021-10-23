package io.ascopes.katana.ap.introspection;

import io.ascopes.katana.ap.commons.StreamableIterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;

/**
 * Iterator across all parent packages for a given element.
 * <p>
 * This follows a bottom-up approach, and will thus include the empty unnamed package
 * last.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class PackageIterator implements StreamableIterator<PackageElement> {
  private final Elements elementUtils;
  private PackageElement next;

  /**
   * @param elementUtils element utils to use for introspection.
   * @param start        the element to start at.
   */
  public PackageIterator(Elements elementUtils, Element start) {
    this.elementUtils = elementUtils;
    this.next = elementUtils.getPackageOf(start);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PackageElement next() throws NoSuchElementException {
    PackageElement current = this.next;

    if (current == null) {
      throw new NoSuchElementException("No parent packages remain to be iterated over");
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
      this.next = ElementUtils.fetchUnnamedPackage(this.elementUtils);
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
  public boolean hasNext() {
    return this.next != null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int characteristics() {
    return Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.DISTINCT;
  }
}