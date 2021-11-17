package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.utils.NamingUtils;
import io.ascopes.katana.ap.utils.ObjectBuilder;
import java.util.Objects;
import java.util.Optional;
import javax.lang.model.element.ExecutableElement;

/**
 * Representation of an attribute within a model.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class Attribute {

  private final String name;
  private final ExecutableElement getterToOverride;
  private final ExecutableElement setterToOverride;

  private Attribute(Builder builder) {
    this.name = Objects.requireNonNull(builder.name);
    this.getterToOverride = Objects.requireNonNull(builder.getterToOverride);
    this.setterToOverride = builder.setterToOverride;
  }

  /**
   * @return the attribute name.
   */
  public String getName() {
    return this.name;
  }

  /**
   * @return the attribute name, modified where appropriate to avoid name clashes with keywords.
   */
  public String getIdentifierName() {
    return NamingUtils.transmogrifyIdentifier(this.name);
  }

  /**
   * @return the getter to override.
   */
  public ExecutableElement getGetterToOverride() {
    return this.getterToOverride;
  }

  /**
   * @return the optional setter to override.
   */
  public Optional<ExecutableElement> getSetterToOverride() {
    return Optional.ofNullable(this.setterToOverride);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends ObjectBuilder<Attribute> {

    private String name;
    private ExecutableElement getterToOverride;
    private ExecutableElement setterToOverride;

    private Builder() {
    }

    public Builder name(String name) {
      this.name = Objects.requireNonNull(name);
      return this;
    }

    public Builder getterToOverride(ExecutableElement getterToOverride) {
      this.getterToOverride = Objects.requireNonNull(getterToOverride);
      return this;
    }

    public Builder setterToOverride(ExecutableElement setterToOverride) {
      this.setterToOverride = Objects.requireNonNull(setterToOverride);
      return this;
    }

    @Override
    public Attribute build() {
      return new Attribute(this);
    }
  }
}
