package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.utils.CollectionUtils;
import io.ascopes.katana.ap.utils.Functors;
import io.ascopes.katana.ap.utils.ObjectBuilder;
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
public final class ClassifiedMethods {

  private final SortedMap<String, ExecutableElement> getters;
  private final SortedMap<String, Set<ExecutableElement>> staticMethods;

  // Nullable attributes
  private final @Nullable ExecutableElement equalsImplementation;
  private final @Nullable ExecutableElement hashCodeImplementation;
  private final @Nullable ExecutableElement toStringImplementation;


  private ClassifiedMethods(Builder builder) {
    this.getters = CollectionUtils.freeze(builder.getters);
    this.staticMethods = CollectionUtils.deepFreeze(builder.staticMethods);

    // Nullable attributes
    this.equalsImplementation = builder.equalsImplementation;
    this.hashCodeImplementation = builder.hashCodeImplementation;
    this.toStringImplementation = builder.toStringImplementation;
  }

  public SortedMap<String, ExecutableElement> getGetters() {
    return this.getters;
  }

  @MaybePresent
  public Optional<ExecutableElement> getEqualsImplementation() {
    return Optional.ofNullable(this.equalsImplementation);
  }

  @MaybePresent
  public Optional<ExecutableElement> getHashCodeImplementation() {
    return Optional.ofNullable(this.hashCodeImplementation);
  }

  @MaybePresent
  public Optional<ExecutableElement> getToStringImplementation() {
    return Optional.ofNullable(this.toStringImplementation);
  }

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

  @MustCall("build")
  public static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("UnusedReturnValue")
  @MustCall("build")
  public static final class Builder implements ObjectBuilder<ClassifiedMethods> {

    private final SortedMap<String, ExecutableElement> getters;
    private final SortedMap<String, Set<ExecutableElement>> staticMethods;

    private @Nullable ExecutableElement equalsImplementation;
    private @Nullable ExecutableElement hashCodeImplementation;
    private @Nullable ExecutableElement toStringImplementation;

    private Builder() {
      this.getters = new TreeMap<>(String::compareTo);
      this.staticMethods = new TreeMap<>(String::compareTo);
    }

    @MaybePresent
    public Optional<ExecutableElement> getExistingGetter(String attributeName) {
      return Optional.ofNullable(this.getters.get(attributeName));
    }

    public Builder getter(String attributeName, ExecutableElement method) {
      Objects.requireNonNull(attributeName);
      Objects.requireNonNull(method);
      this.getters.put(attributeName, method);
      return this;
    }

    public Builder staticMethod(ExecutableElement method) {
      Objects.requireNonNull(method);
      String attributeName = method.getSimpleName().toString();
      this.staticMethods.computeIfAbsent(attributeName, unused -> overloadSet()).add(method);
      return this;
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

    private static Set<ExecutableElement> overloadSet() {
      return new HashSet<>();
    }

    private static SortedMap<String, Set<ExecutableElement>> methodNameMap() {
      return new TreeMap<>(String::compareTo);
    }
  }
}
