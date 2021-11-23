package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import io.ascopes.katana.ap.utils.ObjectBuilder;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedMap;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

/**
 * Descriptor for a model that should be generated.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class Model {

  private final TypeElement modelInterface;
  private final AnnotationMirror annotationMirror;
  private final boolean mutable;
  private final String packageName;
  private final String className;
  private final SettingsCollection settingsCollection;
  private final ClassifiedMethods methods;
  private final SortedMap<String, Attribute> attributes;

  /**
   * @param builder the model builder.
   */
  private Model(Builder builder) {
    this.modelInterface = Objects.requireNonNull(builder.modelInterface);
    this.annotationMirror = Objects.requireNonNull(builder.annotationMirror);
    this.mutable = Objects.requireNonNull(builder.mutable);
    this.packageName = Objects.requireNonNull(builder.packageName);
    this.className = Objects.requireNonNull(builder.className);
    this.settingsCollection = Objects.requireNonNull(builder.settingsCollection);
    this.methods = Objects.requireNonNull(builder.methods);
    this.attributes = Objects.requireNonNull(builder.attributes);
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
   * @return the attributes for the model.
   */
  public SortedMap<String, Attribute> getAttributes() {
    return this.attributes;
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
        "modelInterface='" + this.modelInterface.getQualifiedName() + "', " +
        "mutable=" + this.mutable +
        '}';
  }

  public static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("UnusedReturnValue")
  public static final class Builder extends ObjectBuilder<Model> {

    private @MonotonicNonNull TypeElement modelInterface;
    private @MonotonicNonNull AnnotationMirror annotationMirror;
    private @MonotonicNonNull Boolean mutable;
    private @MonotonicNonNull String packageName;
    private @MonotonicNonNull String className;
    private @MonotonicNonNull SettingsCollection settingsCollection;
    private @MonotonicNonNull ClassifiedMethods methods;
    private @MonotonicNonNull SortedMap<String, Attribute> attributes;

    private Builder() {
    }

    TypeElement getModelInterface() {
      return Objects.requireNonNull(this.modelInterface);
    }

    AnnotationMirror getAnnotationMirror() {
      return Objects.requireNonNull(this.annotationMirror);
    }

    SettingsCollection getSettingsCollection() {
      return Objects.requireNonNull(this.settingsCollection);
    }

    boolean isMutable() {
      return Objects.requireNonNull(this.mutable);
    }

    ClassifiedMethods getMethods() {
      return Objects.requireNonNull(this.methods);
    }

    public Builder modelInterface(TypeElement modelInterface) {
      this.modelInterface = Objects.requireNonNull(modelInterface);
      return this;
    }

    public Builder annotationMirror(AnnotationMirror annotationMirror) {
      this.annotationMirror = Objects.requireNonNull(annotationMirror);
      return this;
    }

    public Builder mutable(Boolean mutable) {
      // Box to enable us to error later if it was not explicitly set.
      this.mutable = Objects.requireNonNull(mutable);
      return this;
    }

    public Builder packageName(String packageName) {
      this.packageName = packageName;
      return this;
    }

    public Builder className(String className) {
      this.className = className;
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

    public Builder attributes(SortedMap<String, Attribute> attributes) {
      Objects.requireNonNull(attributes, "map was null");
      attributes.forEach((key, value) -> {
        Objects.requireNonNull(key, () -> "key was null for value " + value);
        Objects.requireNonNull(value, () -> "value was null for key " + key);
      });
      this.attributes = Collections.unmodifiableSortedMap(attributes);
      return this;
    }

    @Override
    public Model build() {
      return new Model(this);
    }
  }
}
