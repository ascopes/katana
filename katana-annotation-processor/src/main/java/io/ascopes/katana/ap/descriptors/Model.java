package io.ascopes.katana.ap.descriptors;

import java.util.Objects;
import javax.lang.model.element.TypeElement;

/**
 * Descriptor for a model that should be generated.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class Model {
  private String packageName;
  private String className;
  private Boolean mutable;
  private TypeElement modelInterface;

  /**
   * @return the package name.
   */
  public String getPackageName() {
    return Objects.requireNonNull(this.packageName);
  }

  /**
   * @param packageName the package name to set.
   */
  public void setPackageName(String packageName) {
    this.packageName = Objects.requireNonNull(packageName);
  }

  /**
   * @return the class name.
   */
  public String getClassName() {
    return Objects.requireNonNull(this.className);
  }

  /**
   * @param className the class name to set.
   */
  public void setClassName(String className) {
    this.className = Objects.requireNonNull(className);
  }

  /**
   * @return true if the model is mutable, false if it is immutable.
   */
  public boolean isMutable() {
    return Objects.requireNonNull(this.mutable);
  }

  /**
   * @param mutable the mutability flag to set. True implies mutability, false implies immutability.
   */
  public void setMutable(Boolean mutable) {
    this.mutable = Objects.requireNonNull(mutable);
  }

  /**
   * @return the interface TypeElement that the model is being generated from.
   */
  public TypeElement getModelInterface() {
    return Objects.requireNonNull(this.modelInterface);
  }

  /**
   * @param modelInterface the TypeElement that the model is being generated from to set.
   */
  public void setModelInterface(TypeElement modelInterface) {
    this.modelInterface = Objects.requireNonNull(modelInterface);
  }
}
