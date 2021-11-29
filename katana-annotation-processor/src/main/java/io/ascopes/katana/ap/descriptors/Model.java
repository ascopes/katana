package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.settings.gen.SettingsCollection;
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
  private final String qualifiedName;
  private final boolean mutable;
  private final TypeElement superInterface;
  private final AnnotationMirror annotationMirror;
  private final SettingsCollection settingsCollection;
  private final MethodClassification methods;
  private final Set<Constructor> constructors;
  private final SortedSet<Attribute> attributes;

  // Nullable attributes
  private final @Nullable BuilderStrategy builderStrategy;
  private final @Nullable AnnotationMirror deprecatedAnnotation;

  private Model(Builder builder) {
    this.packageName = Objects.requireNonNull(builder.packageName);
    this.className = Objects.requireNonNull(builder.className);
    this.qualifiedName = Objects.requireNonNull(builder.qualifiedName);
    this.mutable = Objects.requireNonNull(builder.mutable);
    this.superInterface = Objects.requireNonNull(builder.superInterface);
    this.annotationMirror = Objects.requireNonNull(builder.annotationMirror);
    this.settingsCollection = Objects.requireNonNull(builder.settingsCollection);
    this.methods = Objects.requireNonNull(builder.methods);

    this.constructors = CollectionUtils.freezeSet(builder.constructors);
    this.attributes = CollectionUtils.freezeSortedSet(builder.attributes);

    // Nullable attributes
    this.builderStrategy = builder.builderStrategy;
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
  public String getQualifiedName() {
    return this.qualifiedName;
  }

  /**
   * Determine whether the model is mutable or not.
   *
   * @return true if mutable, false if immutable.
   */
  public boolean isMutable() {
    return this.mutable;
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
   * Get the settings collection for settings to use with this model.
   *
   * @return the settings collection.
   */
  public SettingsCollection getSettingsCollection() {
    return this.settingsCollection;
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
   * Get the builder strategy to use to generate a builder, if one is enabled.
   *
   * @return the builder strategy, or an empty optional if not enabled.
   */
  @MaybePresent
  public Optional<BuilderStrategy> getBuilderStrategy() {
    return Optional.ofNullable(this.builderStrategy);
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
        + "mutable=" + this.mutable
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
    private @MonotonicNonNull String qualifiedName;
    private @MonotonicNonNull TypeElement superInterface;
    private @MonotonicNonNull Boolean mutable;
    private @MonotonicNonNull AnnotationMirror annotationMirror;
    private @MonotonicNonNull SettingsCollection settingsCollection;
    private @MonotonicNonNull MethodClassification methods;

    // Nullable attributes
    private @Nullable BuilderStrategy builderStrategy;
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
     * Set the qualified name.
     *
     * @param qualifiedName the qualified name to set.
     * @return this builder.
     */
    public Builder qualifiedName(String qualifiedName) {
      this.qualifiedName = Objects.requireNonNull(qualifiedName);
      return this;
    }

    /**
     * Get the super-interface set on this builder.
     *
     * @return the super-interface.
     */
    TypeElement getSuperInterface() {
      return Objects.requireNonNull(this.superInterface);
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
     * Get the annotation mirror for the model annotation that was applied that triggered this model
     * to be built.
     *
     * @return the annotation mirror.
     */
    AnnotationMirror getAnnotationMirror() {
      return Objects.requireNonNull(this.annotationMirror);
    }

    /**
     * Set the annotation mirror for the model annotation that was applied that triggered this model
     * to be built.
     *
     * @param annotationMirror the annotation mirror.
     * @return this builder.
     */
    public Builder annotationMirror(AnnotationMirror annotationMirror) {
      this.annotationMirror = Objects.requireNonNull(annotationMirror);
      return this;
    }

    /**
     * Get the collection of settings that are set in this builder.
     *
     * @return the settings collection.
     */
    SettingsCollection getSettingsCollection() {
      return Objects.requireNonNull(this.settingsCollection);
    }

    /**
     * Set the collection of settings to use for the model.
     *
     * @param settingsCollection the settings collection.
     * @return this builder.
     */
    public Builder settingsCollection(SettingsCollection settingsCollection) {
      this.settingsCollection = settingsCollection;
      return this;
    }

    /**
     * Get the collection of classified methods set in this builder.
     *
     * @return the method classification collection.
     */
    MethodClassification getMethods() {
      return Objects.requireNonNull(this.methods);
    }

    /**
     * Set the classified methods for the model in this builder.
     *
     * @param methods the classified methods to set.
     * @return this builder.
     */
    public Builder methods(MethodClassification methods) {
      this.methods = methods;
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
     * Get whether this model is mutable or not.
     *
     * @return true if mutable, false if immutable.
     */
    boolean isMutable() {
      return Objects.requireNonNull(this.mutable);
    }

    /**
     * Set whether this model is mutable or not.
     *
     * @param mutable true if mutable, false if immutable.
     * @return this builder.
     */
    public Builder mutable(Boolean mutable) {
      // Box to enable us to error later if it was not explicitly set.
      this.mutable = Objects.requireNonNull(mutable);
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
