package io.ascopes.katana.ap.iterators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;

/**
 * Method iterator across the methods declared within a V-Table for an element type.
 *
 * <p>This considers methods in all supertypes and super interfaces, and will ignore any methods
 * that are actively overridden by a method in a more specific implementation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class AvailableMethodsIterator implements StreamableIterator<ExecutableElement> {

  private final Types typeUtils;
  private final Iterator<ExecutableElement> iterator;

  /**
   * Initialize this iterator.
   *
   * <p><strong>Note: </strong> due to the nature of this implementation, the methods are eagerly
   * calculated during construction, rather than lazily as needed.
   *
   * @param typeUtils the type utilities to use for introspection.
   * @param root the type root to start at.
   */
  public AvailableMethodsIterator(Types typeUtils, TypeElement root) {
    // TODO: make this lazy, somehow. If not, this might need profiling perhaps.

    this.typeUtils = typeUtils;
    this.iterator = new SupertypeIterator(typeUtils, root)
        .stream()
        .flatMap(this::methodStreamForType)
        .collect(Collectors.groupingBy(ExecutableElement::getSimpleName))
        .values()
        .stream()
        .flatMap(this::visibleSignaturesForList)
        .iterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExecutableElement next() throws NoSuchElementException {
    return this.iterator.next();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int characteristics() {
    return Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.ORDERED;
  }

  private Stream<ExecutableElement> methodStreamForType(TypeElement type) {
    return ElementFilter
        .methodsIn(type.getEnclosedElements())
        .stream()
        .unordered();
  }

  private Stream<ExecutableElement> visibleSignaturesForList(
      List<ExecutableElement> signatures
  ) {
    List<ExecutableElement> seenSignatures = new ArrayList<>();

    // This runs in O(n²) time, but we shouldn't usually have loads of these, so it isn't too
    // important. We already filtered out methods with different names, so that is the majority
    // of the work done for us.
    outerLoop:
    for (ExecutableElement nextSignature : signatures) {
      ExecutableType nextSignatureType = (ExecutableType) nextSignature.asType();
      for (ExecutableElement seenSignature : seenSignatures) {
        ExecutableType seenSignatureType = (ExecutableType) seenSignature.asType();
        if (this.typeUtils.isSubsignature(nextSignatureType, seenSignatureType)) {
          continue outerLoop;
        }
      }

      seenSignatures.add(nextSignature);
    }

    return seenSignatures
        .stream();
  }
}
