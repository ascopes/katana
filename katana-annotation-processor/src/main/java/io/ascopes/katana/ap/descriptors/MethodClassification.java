package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.utils.CollectionUtils;
import io.ascopes.katana.ap.utils.ObjectBuilder;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.lang.model.element.ExecutableElement;
import org.checkerframework.checker.mustcall.qual.MustCall;
import org.checkerframework.checker.optional.qual.MaybePresent;

/**
 * Holder for methods on an interface, collected by classification.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class MethodClassification {

  private final SortedMap<String, ExecutableElement> getters;
  private final Set<ExecutableElement> staticMethods;

  private MethodClassification(Builder builder) {
    this.getters = CollectionUtils.freezeSortedMap(builder.getters);
    this.staticMethods = CollectionUtils.freezeSet(builder.staticMethods);
  }

  /**
   * Get the known getters for attributes.
   *
   * @return the getters map.
   */
  public SortedMap<String, ExecutableElement> getGetters() {
    return this.getters;
  }

  /**
   * Get the set of static methods.
   *
   * @return the set of static methods.
   */
  public Set<ExecutableElement> getStaticMethods() {
    return this.staticMethods;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {

    return "Methods{"
        + "getters=" + this.getters.values() + ", "
        + "staticMethods=" + this.staticMethods
        + '}';
  }

  /**
   * Create a new builder for a MethodClassification.
   *
   * @return a new builder.
   */
  @MustCall("build")
  public static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("UnusedReturnValue")
  @MustCall("build")
  public static final class Builder implements ObjectBuilder<MethodClassification> {

    private final SortedMap<String, ExecutableElement> getters;
    private final Set<ExecutableElement> staticMethods;

    private Builder() {
      this.getters = new TreeMap<>(String::compareTo);
      this.staticMethods = new HashSet<>();
    }

    /**
     * Get the existing getter in this builder for the given attribute name, if known.
     *
     * @param attributeName the attribute name.
     * @return an optional containing the discovered getter, or empty if not known.
     */
    @MaybePresent
    public Optional<ExecutableElement> getExistingGetter(String attributeName) {
      return Optional.ofNullable(this.getters.get(attributeName));
    }

    /**
     * Add a getter for a given attribute name.
     *
     * @param attributeName the attribute name.
     * @param method        the getter method.
     * @return this builder.
     */
    public Builder getter(String attributeName, ExecutableElement method) {
      Objects.requireNonNull(attributeName);
      Objects.requireNonNull(method);
      this.getters.put(attributeName, method);
      return this;
    }

    /**
     * Add a static method.
     *
     * @param method the static method.
     * @return this builder.
     */
    public Builder staticMethod(ExecutableElement method) {
      Objects.requireNonNull(method);
      this.staticMethods.add(method);
      return this;
    }

    /**
     * Build a MethodClassification from this builder.
     *
     * @return the generated method classification.
     */
    public MethodClassification build() {
      return new MethodClassification(this);
    }
  }
}
