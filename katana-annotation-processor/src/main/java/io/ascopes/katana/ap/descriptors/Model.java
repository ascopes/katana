package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import io.ascopes.katana.ap.utils.ObjectBuilder;
import java.util.Objects;
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

  /**
   * @param builder the model builder.
   */
  private Model(Builder builder) {
    this.modelInterface = builder.getModelInterface();
    this.annotationMirror = builder.getAnnotationMirror();
    this.mutable = builder.isMutable();
    this.packageName = builder.getPackageName();
    this.className = builder.getClassName();
    this.settingsCollection = builder.getSettingsCollection();
    this.methods = builder.getMethods();
  }

  /**
   * @return the interface TypeElement that the model is being generated from.
   */
  public TypeElement getModelInterface() {
    return this.modelInterface;
  }

  /**
   * @return the model annotation mirror (either ImmutableModel or MutableModel).
   */
  public AnnotationMirror getAnnotationMirror() {
    return this.annotationMirror;
  }

  /**
   * @return true if a mutable model, false if an immutable model.
   */
  public boolean isMutable() {
    return this.mutable;
  }

  @Override
  public String toString() {
    return "Model{" +
        "packageName='" + this.packageName + "', " +
        "className='" + this.className + "', " +
        "methods=" + this.methods + ", " +
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

    private Builder() {
    }

    public TypeElement getModelInterface() {
      return Objects.requireNonNull(this.modelInterface);
    }

    public AnnotationMirror getAnnotationMirror() {
      return Objects.requireNonNull(this.annotationMirror);
    }

    public SettingsCollection getSettingsCollection() {
      return Objects.requireNonNull(this.settingsCollection);
    }

    public boolean isMutable() {
      return Objects.requireNonNull(this.mutable);
    }

    public String getPackageName() {
      return Objects.requireNonNull(this.packageName);
    }

    public String getClassName() {
      return Objects.requireNonNull(this.className);
    }

    public ClassifiedMethods getMethods() {
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

    @Override
    public Model build() {
      return new Model(this);
    }
  }
}
