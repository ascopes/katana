package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import io.ascopes.katana.ap.utils.ObjectBuilder;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Descriptor for a model that should be generated.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class Model {

  private final String packageName;
  private final String className;
  private final boolean mutable;
  private final TypeElement superInterface;
  private final AnnotationMirror annotationMirror;
  private final SettingsCollection settingsCollection;
  private final ClassifiedMethods methods;
  private final SortedSet<Attribute> attributes;

  // Nullable attributes
  private final @Nullable AnnotationMirror deprecatedAnnotation;

  /**
   * @param builder the model builder.
   */
  private Model(Builder builder) {
    this.packageName = Objects.requireNonNull(builder.packageName);
    this.className = Objects.requireNonNull(builder.className);
    this.mutable = Objects.requireNonNull(builder.mutable);
    this.superInterface = Objects.requireNonNull(builder.superInterface);
    this.annotationMirror = Objects.requireNonNull(builder.annotationMirror);
    this.settingsCollection = Objects.requireNonNull(builder.settingsCollection);
    this.methods = Objects.requireNonNull(builder.methods);
    this.attributes = Objects.requireNonNull(builder.attributes);

    // Nullable attributes
    this.deprecatedAnnotation = builder.deprecatedAnnotation;
  }

  /**
   * @return the package name for the model.
   */
  public String getPackageName() {
    return this.packageName;
  }

  /**
   * @return the class name for the model.
   */
  public String getClassName() {
    return this.className;
  }

  /**
   * @return true if the type is mutable, false if it is not mutable.
   */
  public boolean isMutable() {
    return this.mutable;
  }

  /**
   * @return the superinterface to inherit from. This is the interface that the model was declared
   * from.
   */
  public TypeElement getSuperInterface() {
    return this.superInterface;
  }

  /**
   * @return the settings for the model.
   */
  public SettingsCollection getSettingsCollection() {
    return this.settingsCollection;
  }

  /**
   * @return the attributes for the model.
   */
  public SortedSet<Attribute> getAttributes() {
    return this.attributes;
  }

  /**
   * @return the deprecated annotation, if present.
   */
  public Optional<AnnotationMirror> getDeprecatedAnnotation() {
    return Optional.ofNullable(this.deprecatedAnnotation);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "Model{" +
        "packageName='" + this.packageName + "', " +
        "className='" + this.className + "', " +
        "attributes=" + this.attributes + ", " +
        "superInterface='" + this.superInterface.getQualifiedName() + "', " +
        "mutable=" + this.mutable +
        '}';
  }

  public static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("UnusedReturnValue")
  public static final class Builder implements ObjectBuilder<Model> {

    private @MonotonicNonNull String packageName;
    private @MonotonicNonNull String className;
    private @MonotonicNonNull TypeElement superInterface;
    private @MonotonicNonNull Boolean mutable;
    private @MonotonicNonNull AnnotationMirror annotationMirror;
    private @MonotonicNonNull SettingsCollection settingsCollection;
    private @MonotonicNonNull ClassifiedMethods methods;
    private @MonotonicNonNull SortedSet<Attribute> attributes;

    // Nullable attributes
    private @Nullable AnnotationMirror deprecatedAnnotation;

    private Builder() {
    }

    TypeElement getSuperInterface() {
      return Objects.requireNonNull(this.superInterface);
    }

    boolean isMutable() {
      return Objects.requireNonNull(this.mutable);
    }

    AnnotationMirror getAnnotationMirror() {
      return Objects.requireNonNull(this.annotationMirror);
    }

    SettingsCollection getSettingsCollection() {
      return Objects.requireNonNull(this.settingsCollection);
    }

    ClassifiedMethods getMethods() {
      return Objects.requireNonNull(this.methods);
    }

    public Builder packageName(String packageName) {
      this.packageName = packageName;
      return this;
    }

    public Builder className(String className) {
      this.className = className;
      return this;
    }

    public Builder mutable(Boolean mutable) {
      // Box to enable us to error later if it was not explicitly set.
      this.mutable = Objects.requireNonNull(mutable);
      return this;
    }

    public Builder superInterface(TypeElement superInterface) {
      this.superInterface = Objects.requireNonNull(superInterface);
      return this;
    }

    public Builder annotationMirror(AnnotationMirror annotationMirror) {
      this.annotationMirror = Objects.requireNonNull(annotationMirror);
      return this;
    }

    public Builder settingsCollection(SettingsCollection settingsCollection) {
      this.settingsCollection = settingsCollection;
      return this;
    }

    public Builder methods(ClassifiedMethods methods) {
      this.methods = methods;
      return this;
    }

    public Builder attributes(SortedSet<Attribute> attributes) {
      Objects.requireNonNull(attributes, "attribute set was null");
      attributes.forEach(element -> Objects.requireNonNull(element, "attribute was null"));
      this.attributes = Collections.unmodifiableSortedSet(attributes);
      return this;
    }

    public Builder deprecatedAnnotation(@Nullable AnnotationMirror deprecatedAnnotation) {
      this.deprecatedAnnotation = deprecatedAnnotation;
      return this;
    }

    @Override
    public Model build() {
      return new Model(this);
    }
  }
}
