package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import io.ascopes.katana.ap.utils.ObjectBuilder;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedMap;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

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

    private TypeElement modelInterface;
    private AnnotationMirror annotationMirror;
    private Boolean mutable;
    private String packageName;
    private String className;
    private SettingsCollection settingsCollection;
    private ClassifiedMethods methods;
    private SortedMap<String, Attribute> attributes;

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

    public Builder mutable(boolean mutable) {
      // Box to enable us to error later if it was not explicitly set.
      this.mutable = mutable;
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
      attributes.forEach((k, v) -> Objects.requireNonNull(v, "Value for key " + k + " was null"));
      this.attributes = Collections.unmodifiableSortedMap(attributes);
      return this;
    }

    @Override
    public Model build() {
      return new Model(this);
    }
  }
}
