package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.utils.CollectionUtils;
import io.ascopes.katana.ap.utils.Functors;
import io.ascopes.katana.ap.utils.ObjectBuilder;
import io.ascopes.katana.ap.utils.StringUtils;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.lang.model.element.ExecutableElement;
import org.checkerframework.checker.mustcall.qual.MustCall;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.optional.qual.MaybePresent;

/**
 * Holder for methods on an interface, collected by classification.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class MethodClassification {

  private final SortedMap<String, ExecutableElement> getters;
  private final SortedMap<String, Set<ExecutableElement>> staticMethods;

  // Nullable attributes
  private final @Nullable ExecutableElement equalsImplementation;
  private final @Nullable ExecutableElement hashCodeImplementation;
  private final @Nullable ExecutableElement toStringImplementation;

  private MethodClassification(Builder builder) {
    this.getters = CollectionUtils.freezeSortedMap(builder.getters);
    this.staticMethods = CollectionUtils.freezeSortedMapOfSets(builder.staticMethods);

    // Nullable attributes
    this.equalsImplementation = builder.equalsImplementation;
    this.hashCodeImplementation = builder.hashCodeImplementation;
    this.toStringImplementation = builder.toStringImplementation;
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
   * Get the known static implementation of an equality method.
   *
   * @return the known static implementation of an equality method, or an empty optional if not
   *     known.
   */
  @MaybePresent
  public Optional<ExecutableElement> getEqualsImplementation() {
    return Optional.ofNullable(this.equalsImplementation);
  }


  /**
   * Get the known static implementation of a hashCode method.
   *
   * @return the known static implementation of a hashCode method, or an empty optional if not
   *     known.
   */
  @MaybePresent
  public Optional<ExecutableElement> getHashCodeImplementation() {
    return Optional.ofNullable(this.hashCodeImplementation);
  }

  /**
   * Get the known static implementation of a toString method.
   *
   * @return the known static implementation of a toString method, or an empty optional if not
   *     known.
   */
  @MaybePresent
  public Optional<ExecutableElement> getToStringImplementation() {
    return Optional.ofNullable(this.toStringImplementation);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    String getters = this.getters
        .values()
        .toString();

    String staticMethods = this.staticMethods
        .values()
        .stream()
        .flatMap(Functors.flattenCollection())
        .collect(Collectors.toSet())
        .toString();

    return "Methods{"
        + "getters=" + getters + ", "
        + "equalsImplementation=" + StringUtils.quoted(this.equalsImplementation) + ", "
        + "hashCodeImplementation=" + StringUtils.quoted(this.hashCodeImplementation) + ", "
        + "toStringImplementation=" + StringUtils.quoted(this.toStringImplementation) + ", "
        + "staticMethods=" + staticMethods
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
    private final SortedMap<String, Set<ExecutableElement>> staticMethods;

    private @Nullable ExecutableElement equalsImplementation;
    private @Nullable ExecutableElement hashCodeImplementation;
    private @Nullable ExecutableElement toStringImplementation;

    private Builder() {
      this.getters = new TreeMap<>(String::compareTo);
      this.staticMethods = new TreeMap<>(String::compareTo);
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
      String attributeName = method.getSimpleName().toString();
      this.staticMethods.computeIfAbsent(attributeName, unused -> overloadSet()).add(method);
      return this;
    }

    /**
     * Set the static equals implementation to use.
     *
     * @param equalsImplementation the nullable static equals implementation to use.
     * @return this builder.
     */
    public Builder equalsImplementation(@Nullable ExecutableElement equalsImplementation) {
      this.equalsImplementation = equalsImplementation;
      return this;
    }

    /**
     * Set the static hashCode implementation to use.
     *
     * @param hashCodeImplementation the nullable static hashCode implementation to use.
     * @return this builder.
     */
    public Builder hashCodeImplementation(@Nullable ExecutableElement hashCodeImplementation) {
      this.hashCodeImplementation = hashCodeImplementation;
      return this;
    }

    /**
     * Set the static toString implementation to use.
     *
     * @param toStringImplementation the nullable static toString implementation to use.
     * @return this builder.
     */
    public Builder toStringImplementation(@Nullable ExecutableElement toStringImplementation) {
      this.toStringImplementation = toStringImplementation;
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

    private static Set<ExecutableElement> overloadSet() {
      return new HashSet<>();
    }
  }
}
