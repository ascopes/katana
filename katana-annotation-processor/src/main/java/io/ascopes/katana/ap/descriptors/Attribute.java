package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.utils.NamingUtils;
import io.ascopes.katana.ap.utils.ObjectBuilder;
import java.util.Objects;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Representation of an attribute within a model.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class Attribute {

  private final String name;
  private final ExecutableElement getterToOverride;
  private final @Nullable ExecutableElement setterToOverride;
  private final boolean setterEnabled;
  private final boolean includeInToString;
  private final boolean includeInEqualsAndHashCode;

  private Attribute(Builder builder) {
    this.name = Objects.requireNonNull(builder.name);
    this.getterToOverride = Objects.requireNonNull(builder.getterToOverride);
    this.setterToOverride = builder.setterToOverride;
    this.setterEnabled = Objects.requireNonNull(builder.setterEnabled);
    this.includeInToString = Objects.requireNonNull(builder.includeInToString);
    this.includeInEqualsAndHashCode = Objects.requireNonNull(builder.includeInEqualsAndHashCode);
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
  // TODO: remove suppression later
  @SuppressWarnings("unused")
  public String getIdentifierName() {
    return NamingUtils.transmogrifyIdentifier(this.name);
  }

  /**
   * @return the attribute type.
   */
  public TypeMirror getType() {
    return this.getterToOverride.getReturnType();
  }

  @Override
  public String toString() {
    return "Attribute{" +
        "name='" + this.name + "', " +
        "type='" + this.getterToOverride.getReturnType() + "', " +
        "get=true, set=" + this.setterEnabled +
        '}';
  }

  public static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("UnusedReturnValue")
  public static final class Builder extends ObjectBuilder<Attribute> {

    private @MonotonicNonNull String name;
    private @MonotonicNonNull ExecutableElement getterToOverride;
    private @Nullable ExecutableElement setterToOverride;
    private @MonotonicNonNull Boolean setterEnabled;
    private @MonotonicNonNull Boolean includeInToString;
    private @MonotonicNonNull Boolean includeInEqualsAndHashCode;

    private Builder() {
    }

    String getName() {
      return Objects.requireNonNull(this.name);
    }

    ExecutableElement getGetterToOverride() {
      return Objects.requireNonNull(this.getterToOverride);
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

    public Builder setterEnabled(boolean setterEnabled) {
      this.setterEnabled = setterEnabled;
      return this;
    }

    public Builder includeInToString(boolean includeInToString) {
      this.includeInToString = includeInToString;
      return this;
    }

    public Builder includeInEqualsAndHashCode(boolean includeInEqualsAndHashCode) {
      this.includeInEqualsAndHashCode = includeInEqualsAndHashCode;
      return this;
    }

    @Override
    public Attribute build() {
      return new Attribute(this);
    }
  }
}
