package io.ascopes.katana.ap.descriptors;

import java.util.Objects;
import javax.lang.model.element.TypeElement;

public class Model {
  private String packageName;
  private String className;
  private Boolean mutable;
  private TypeElement modelInterface;

  public String getPackageName() {
    return Objects.requireNonNull(this.packageName);
  }

  public void setPackageName(String packageName) {
    this.packageName = Objects.requireNonNull(packageName);
  }

  public String getClassName() {
    return Objects.requireNonNull(this.className);
  }

  public void setClassName(String className) {
    this.className = Objects.requireNonNull(className);
  }

  public boolean isMutable() {
    return Objects.requireNonNull(this.mutable);
  }

  public void setMutable(Boolean mutable) {
    this.mutable = Objects.requireNonNull(mutable);
  }

  public TypeElement getModelInterface() {
    return Objects.requireNonNull(this.modelInterface);
  }

  public void setModelInterface(TypeElement modelInterface) {
    this.modelInterface = Objects.requireNonNull(modelInterface);
  }
}
