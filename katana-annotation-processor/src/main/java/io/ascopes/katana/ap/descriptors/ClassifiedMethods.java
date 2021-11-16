package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.utils.Functors;
import io.ascopes.katana.ap.utils.ObjectBuilder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.lang.model.element.ExecutableElement;

/**
 * Holder for methods on an interface, collected by classification.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class ClassifiedMethods {

  private final SortedMap<String, ExecutableElement> getters;
  private final SortedMap<String, ExecutableElement> setters;
  private final SortedMap<String, Set<ExecutableElement>> otherInstanceMethods;
  private final SortedMap<String, Set<ExecutableElement>> staticMethods;

  private ClassifiedMethods(Builder builder) {
    this.getters = Collections.unmodifiableSortedMap(builder.getGetters());
    this.setters = Collections.unmodifiableSortedMap(builder.getSetters());
    this.otherInstanceMethods = deepImmutableOverloads(builder.getOtherInstanceMethods());
    this.staticMethods = deepImmutableOverloads(builder.getStaticMethods());
  }

  /**
   * @return the getters, mapping the raw attribute name to the getter element.
   */
  public SortedMap<String, ExecutableElement> getGetters() {
    return this.getters;
  }

  /**
   * @return the setters, mapping the raw attribute name to the setter element.
   */
  public SortedMap<String, ExecutableElement> getSetters() {
    return this.setters;
  }

  /**
   * @return any other instance methods.
   */
  public SortedMap<String, Set<ExecutableElement>> getOtherInstanceMethods() {
    return this.otherInstanceMethods;
  }

  /**
   * @return any static instance methods.
   */
  public SortedMap<String, Set<ExecutableElement>> getStaticMethods() {
    return this.staticMethods;
  }

  /**
   * @return a string representation of this collection.
   */
  @Override
  public String toString() {
    String getters = this.getters
        .values()
        .toString();

    String setters = this.setters
        .values()
        .toString();

    String otherInstanceMethods = this.otherInstanceMethods
        .values()
        .stream()
        .flatMap(Functors.flattenCollection())
        .collect(Collectors.toSet())
        .toString();

    String staticMethods = this.staticMethods
        .values()
        .stream()
        .flatMap(Functors.flattenCollection())
        .collect(Collectors.toSet())
        .toString();

    return "Methods{" +
        "getters=" + getters + ", " +
        "setters=" + setters + ", " +
        "otherInstanceMethods=" + otherInstanceMethods + ", " +
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
    elements.forEach((key, values) -> elementsCopy.put(key, Collections.unmodifiableSet(values)));
    return Collections.unmodifiableSortedMap(elementsCopy);
  }

  /**
   * Builder for method classifications.
   */
  @SuppressWarnings("UnusedReturnValue")
  public static final class Builder extends ObjectBuilder<ClassifiedMethods> {

    private final SortedMap<String, ExecutableElement> getters;
    private final SortedMap<String, ExecutableElement> setters;
    private final SortedMap<String, Set<ExecutableElement>> otherInstanceMethods;
    private final SortedMap<String, Set<ExecutableElement>> staticMethods;

    private Builder() {
      this.getters = new MethodNameMap<>();
      this.setters = new MethodNameMap<>();
      this.otherInstanceMethods = new MethodNameMap<>();
      this.staticMethods = new MethodNameMap<>();
    }

    /**
     * @return all getters in the builder.
     */
    public SortedMap<String, ExecutableElement> getGetters() {
      return this.getters;
    }

    /**
     * @return all setters in the builder.
     */
    public SortedMap<String, ExecutableElement> getSetters() {
      return this.setters;
    }

    /**
     * @return all unclassified instance methods in the builder.
     */
    public SortedMap<String, Set<ExecutableElement>> getOtherInstanceMethods() {
      return this.otherInstanceMethods;
    }

    /**
     * @return all static methods in the builder.
     */
    public SortedMap<String, Set<ExecutableElement>> getStaticMethods() {
      return this.staticMethods;
    }

    /**
     * Add a getter.
     *
     * @param attributeName the attribute name that the getter applies to.
     * @param method        the getter definition.
     * @return this builder.
     */
    public Builder getter(String attributeName, ExecutableElement method) {
      return this.put(this.getters, attributeName, method);
    }

    /**
     * Add a setter.
     *
     * @param attributeName the attribute name that the setter applies to.
     * @param method        the setter definition.
     * @return this builder.
     */
    public Builder setter(String attributeName, ExecutableElement method) {
      return this.put(this.setters, attributeName, method);
    }

    /**
     * Add an unclassified instance method.
     *
     * @param method the method definition.
     * @return this builder.
     */
    public Builder instanceMethod(ExecutableElement method) {
      return this.put(this.otherInstanceMethods, method);
    }

    /**
     * Add a static method.
     *
     * @param method the method definition.
     * @return this builder.
     */
    public Builder staticMethod(ExecutableElement method) {
      return this.put(this.staticMethods, method);
    }

    /**
     * Build the method classification and return it.
     */
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
