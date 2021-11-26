package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.utils.Functors;
import io.ascopes.katana.ap.utils.ObjectBuilder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.lang.model.element.ExecutableElement;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.optional.qual.MaybePresent;

/**
 * Holder for methods on an interface, collected by classification.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class ClassifiedMethods {

  private final SortedMap<String, ExecutableElement> getters;
  private final SortedMap<String, Set<ExecutableElement>> staticMethods;

  // Nullable attributes
  private final @Nullable ExecutableElement equalsImplementation;
  private final @Nullable ExecutableElement hashCodeImplementation;
  private final @Nullable ExecutableElement toStringImplementation;


  private ClassifiedMethods(Builder builder) {
    this.getters = unmodifiableSortedMap(builder.getters);
    this.staticMethods = deepImmutableOverloads(builder.staticMethods);

    // Nullable attributes
    this.equalsImplementation = builder.equalsImplementation;
    this.hashCodeImplementation = builder.hashCodeImplementation;
    this.toStringImplementation = builder.toStringImplementation;
  }

  /**
   * @return the getters, mapping the raw attribute name to the getter element.
   */
  public SortedMap<String, ExecutableElement> getGetters() {
    return this.getters;
  }

  /**
   * @return the custom static equality implementation to use, if provided.
   */
  @MaybePresent
  public Optional<ExecutableElement> getEqualsImplementation() {
    return Optional.ofNullable(this.equalsImplementation);
  }

  /**
   * @return the custom static hashCode implementation to use, if provided.
   */
  @MaybePresent
  public Optional<ExecutableElement> getHashCodeImplementation() {
    return Optional.ofNullable(this.hashCodeImplementation);
  }

  /**
   * @return the custom static toString implementation to use, if provided.
   */
  @MaybePresent
  public Optional<ExecutableElement> getToStringImplementation() {
    return Optional.ofNullable(this.toStringImplementation);
  }

  /**
   * @return a string representation of this collection.
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

    return "Methods{" +
        "getters=" + getters + ", " +
        "equalsImplementation=" + this.equalsImplementation + ", " +
        "hashCodeImplementation=" + this.hashCodeImplementation + ", " +
        "toStringImplementation=" + this.toStringImplementation + ", " +
        "staticMethods=" + staticMethods +
        '}';
  }

  /**
   * Initialize a builder for a method classification.
   *
   * @return the builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  private static SortedMap<String, Set<ExecutableElement>> deepImmutableOverloads(
      SortedMap<String, Set<ExecutableElement>> elements
  ) {
    SortedMap<String, Set<ExecutableElement>> elementsCopy = new TreeMap<>(String::compareTo);
    elements.forEach((key, values) -> {
      Objects.requireNonNull(values, () -> "set was null for " + key);
      values.forEach(item -> Objects.requireNonNull(item, () -> "null item in set for " + key));
      elementsCopy.put(key, Collections.unmodifiableSet(values));
    });
    return unmodifiableSortedMap(elementsCopy);
  }

  private static <K, V> SortedMap<K, V> unmodifiableSortedMap(SortedMap<K, V> map) {
    Objects.requireNonNull(map, "Map was null");
    map.forEach((key, value) -> {
      Objects.requireNonNull(key, () -> "key was null for value " + value);
      Objects.requireNonNull(value, () -> "value was null for key " + key);
    });
    return Collections.unmodifiableSortedMap(map);
  }

  /**
   * Builder for method classifications.
   */
  @SuppressWarnings("UnusedReturnValue")
  public static final class Builder implements ObjectBuilder<ClassifiedMethods> {

    private final SortedMap<String, ExecutableElement> getters;
    private final SortedMap<String, Set<ExecutableElement>> staticMethods;

    private @Nullable ExecutableElement equalsImplementation;
    private @Nullable ExecutableElement hashCodeImplementation;
    private @Nullable ExecutableElement toStringImplementation;

    private Builder() {
      this.getters = new MethodNameMap<>();
      this.staticMethods = new MethodNameMap<>();
    }

    @MaybePresent
    public Optional<ExecutableElement> getExistingGetter(String attributeName) {
      return Optional.ofNullable(this.getters.get(attributeName));
    }

    public Builder getter(String attributeName, ExecutableElement method) {
      return this.put(this.getters, attributeName, method);
    }

    public Builder staticMethod(ExecutableElement method) {
      return this.put(this.staticMethods, method);
    }

    public Builder equalsImplementation(@Nullable ExecutableElement equalsImplementation) {
      this.equalsImplementation = equalsImplementation;
      return this;
    }

    public Builder hashCodeImplementation(@Nullable ExecutableElement hashCodeImplementation) {
      this.hashCodeImplementation = hashCodeImplementation;
      return this;
    }

    public Builder toStringImplementation(@Nullable ExecutableElement toStringImplementation) {
      this.toStringImplementation = toStringImplementation;
      return this;
    }

    public ClassifiedMethods build() {
      return new ClassifiedMethods(this);
    }

    private Builder put(
        SortedMap<String, ExecutableElement> map,
        String attributeName,
        ExecutableElement method
    ) {
      Objects.requireNonNull(attributeName);
      Objects.requireNonNull(method);
      map.put(attributeName, method);
      return this;
    }

    private Builder put(
        SortedMap<String, Set<ExecutableElement>> map,
        ExecutableElement method
    ) {
      String attributeName = method.getSimpleName().toString();
      Objects.requireNonNull(method);
      map.computeIfAbsent(attributeName, unused -> new MethodOverloadSet())
          .add(method);
      return this;
    }
  }

  private static final class MethodNameMap<V> extends TreeMap<String, V> {

    // Type alias
    private MethodNameMap() {
      super(String::compareTo);
    }
  }

  private static final class MethodOverloadSet extends HashSet<ExecutableElement> {
    // Type alias
  }
}
