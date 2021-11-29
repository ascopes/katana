package io.ascopes.katana.ap.descriptors;

import com.squareup.javapoet.ClassName;
import io.ascopes.katana.ap.utils.CollectionUtils;
import io.ascopes.katana.ap.utils.ObjectBuilder;
import io.ascopes.katana.ap.utils.StringUtils;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.checkerframework.checker.mustcall.qual.MustCall;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.optional.qual.MaybePresent;

/**
 * Descriptor for a model that should be generated.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class Model {

  private final String packageName;
  private final String className;
  private final TypeElement superInterface;
  private final Set<Constructor> constructors;
  private final SortedSet<Attribute> attributes;
  private final String indent;
  private final String setterPrefix;

  // Nullable attributes
  private final @Nullable BuilderStrategy builderStrategy;
  private final @Nullable EqualityStrategy equalityStrategy;
  private final @Nullable AnnotationMirror deprecatedAnnotation;

  private Model(Builder builder) {
    this.packageName = Objects.requireNonNull(builder.packageName);
    this.className = Objects.requireNonNull(builder.className);
    this.superInterface = Objects.requireNonNull(builder.superInterface);
    this.indent = Objects.requireNonNull(builder.indent);
    this.setterPrefix = Objects.requireNonNull(builder.setterPrefix);

    // Collection attributes.
    this.constructors = CollectionUtils.freezeSet(builder.constructors);
    this.attributes = CollectionUtils.freezeSortedSet(builder.attributes);

    // Nullable attributes.
    this.builderStrategy = builder.builderStrategy;
    this.equalityStrategy = builder.equalityStrategy;
    this.deprecatedAnnotation = builder.deprecatedAnnotation;
  }

  /**
   * Get the package name for the generated model.
   *
   * @return the package name for the generated model.
   */
  public String getPackageName() {
    return this.packageName;
  }

  /**
   * Get the class name for the generated model.
   *
   * @return the class name for the generated model.
   */
  public String getClassName() {
    return this.className;
  }

  /**
   * Get the qualified name for the generated model.
   *
   * @return the qualified name for the generated model.
   */
  public ClassName getQualifiedName() {
    return ClassName.get(this.packageName, this.className);
  }

  /**
   * Get the super-interface that defined this model.
   *
   * @return the super-interface that defined this model.
   */
  public TypeElement getSuperInterface() {
    return this.superInterface;
  }

  /**
   * Get the set of constructors to use for this model.
   *
   * @return the set of constructors.
   */
  public Set<Constructor> getConstructors() {
    return this.constructors;
  }

  /**
   * Get the attributes declared on this model.
   *
   * @return the attributes declared on this model.
   */
  public SortedSet<Attribute> getAttributes() {
    return this.attributes;
  }

  /**
   * Get the indent for the generated code.
   *
   * @return the indent for the generated code.
   */
  public String getIndent() {
    return this.indent;
  }

  /**
   * Get the setter prefix for the generated code.
   *
   * @return the setter prefix for the generated code.
   */
  public String getSetterPrefix() {
    return this.setterPrefix;
  }

  /**
   * Get the builder strategy to use to generate a builder, if one is enabled.
   *
   * @return the builder strategy, or an empty optional if not enabled.
   */
  @MaybePresent
  public Optional<BuilderStrategy> getBuilderStrategy() {
    return Optional.ofNullable(this.builderStrategy);
  }

  /**
   * Get the equality strategy to use to generate an equals and hashcode method, if one is enabled.
   *
   * @return the equality strategy, or an empty optional if not enabled.
   */
  @MaybePresent
  public Optional<EqualityStrategy> getEqualityStrategy() {
    return Optional.ofNullable(this.equalityStrategy);
  }

  /**
   * Get the deprecated annotation to apply to the generated model type, if one is provided on the
   * super-interface.
   *
   * @return the deprecated annotation, or an empty optional if not provided.
   */
  @MaybePresent
  public Optional<AnnotationMirror> getDeprecatedAnnotation() {
    return Optional.ofNullable(this.deprecatedAnnotation);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "Model{"
        + "packageName=" + StringUtils.quoted(this.packageName) + ", "
        + "className=" + StringUtils.quoted(this.className) + ", "
        + "attributes=" + this.attributes + ", "
        + "superInterface=" + StringUtils.quoted(this.superInterface.getQualifiedName()) + ", "
        + '}';
  }

  /**
   * Create a builder for a new model.
   *
   * @return the builder.
   */
  @MustCall("build")
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for Model objects.
   */
  @SuppressWarnings("UnusedReturnValue")
  @MustCall("build")
  public static final class Builder implements ObjectBuilder<Model> {

    private final Set<Constructor> constructors;
    private final SortedSet<Attribute> attributes;

    private @MonotonicNonNull String packageName;
    private @MonotonicNonNull String className;
    private @MonotonicNonNull TypeElement superInterface;
    private @MonotonicNonNull String indent;
    private @MonotonicNonNull String setterPrefix;

    // Nullable attributes
    private @Nullable BuilderStrategy builderStrategy;
    private @Nullable EqualityStrategy equalityStrategy;
    private @Nullable AnnotationMirror deprecatedAnnotation;

    private Builder() {
      this.constructors = new HashSet<>();
      this.attributes = new TreeSet<>(Comparator.comparing(Attribute::getName));
    }

    /**
     * Set the package name.
     *
     * <p>Empty packages are denoted by an empty string {@code ""}.
     *
     * @param packageName the package name to set.
     * @return this builder.
     */
    public Builder packageName(String packageName) {
      this.packageName = Objects.requireNonNull(packageName);
      return this;
    }

    /**
     * Set the class name.
     *
     * @param className the class name to set.
     * @return this builder.
     */
    public Builder className(String className) {
      this.className = Objects.requireNonNull(className);
      return this;
    }

    /**
     * Set the super-interface that defined this model.
     *
     * @param superInterface the super-interface.
     * @return this builder.
     */
    public Builder superInterface(TypeElement superInterface) {
      this.superInterface = Objects.requireNonNull(superInterface);
      return this;
    }

    /**
     * Add an attribute.
     *
     * @param attribute the attribute to add.
     * @return this builder.
     */
    public Builder attribute(Attribute attribute) {
      Objects.requireNonNull(attribute);
      this.attributes.add(attribute);
      return this;
    }

    /**
     * Add a constructor.
     *
     * @param constructor the constructor to add.
     * @return this builder.
     */
    public Builder constructor(Constructor constructor) {
      Objects.requireNonNull(constructor);
      this.constructors.add(constructor);
      return this;
    }

    /**
     * Set the indent for output code.
     *
     * @param indent the indent to use.
     * @return this builder.
     */
    public Builder indent(String indent) {
      this.indent = Objects.requireNonNull(indent);
      return this;
    }

    /**
     * Set the setter prefix to use.
     *
     * @param setterPrefix the setter prefix to use.
     * @return this builder.
     */
    public Builder setterPrefix(String setterPrefix) {
      this.setterPrefix = Objects.requireNonNull(setterPrefix);
      return this;
    }

    /**
     * Set the builder strategy.
     *
     * @param builderStrategy the nullable builder strategy to set.
     * @return this builder.
     */
    public Builder builderStrategy(@Nullable BuilderStrategy builderStrategy) {
      this.builderStrategy = builderStrategy;
      return this;
    }

    /**
     * Set the equality strategy.
     *
     * @param equalityStrategy the nullable equality strategy to set.
     * @return this builder.
     */
    public Builder equalityStrategy(@Nullable EqualityStrategy equalityStrategy) {
      this.equalityStrategy = equalityStrategy;
      return this;
    }

    /**
     * Set the model-level deprecation annotation.
     *
     * @param deprecatedAnnotation the nullable annotation mirror to set.
     * @return this builder.
     */
    public Builder deprecatedAnnotation(@Nullable AnnotationMirror deprecatedAnnotation) {
      this.deprecatedAnnotation = deprecatedAnnotation;
      return this;
    }

    /**
     * Build a Model instance from this builder.
     *
     * @return the constructed Model object.
     */
    @Override
    public Model build() {
      return new Model(this);
    }
  }
}
