package io.ascopes.katana.ap.descriptors;

import com.squareup.javapoet.TypeName;
import io.ascopes.katana.annotations.Visibility;
import io.ascopes.katana.ap.utils.ObjectBuilder;
import java.util.Objects;
import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
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
  private final String identifier;
  private final TypeName type;
  private final Visibility fieldVisibility;
  private final boolean final_;
  private final boolean transient_;
  private final ExecutableElement getterToOverride;
  private final boolean setterEnabled;
  private final boolean includeInToString;
  private final boolean includeInEqualsAndHashCode;

  // Nullable fields
  private final @Nullable AnnotationMirror deprecatedAnnotation;


  private Attribute(Builder builder) {
    this.name = Objects.requireNonNull(builder.name);
    this.identifier = Objects.requireNonNull(builder.identifier);
    this.type = Objects.requireNonNull(builder.type);
    this.fieldVisibility = Objects.requireNonNull(builder.fieldVisibility);
    this.final_ = Objects.requireNonNull(builder.final_);
    this.transient_ = Objects.requireNonNull(builder.transient_);
    this.getterToOverride = Objects.requireNonNull(builder.getterToOverride);
    this.setterEnabled = Objects.requireNonNull(builder.setterEnabled);
    this.includeInToString = Objects.requireNonNull(builder.includeInToString);
    this.includeInEqualsAndHashCode = Objects.requireNonNull(builder.includeInEqualsAndHashCode);

    // Nullable fields
    this.deprecatedAnnotation = builder.deprecatedAnnotation;
  }

  /**
   * @return the raw name of the attribute. This may not be safe for use in actual identifiers.
   */
  public String getName() {
    return this.name;
  }

  /**
   * @return the attribute name, modified where appropriate to avoid name clashes with keywords.
   */
  public String getIdentifier() {
    return this.identifier;
  }

  /**
   * @return the attribute type.
   */
  public TypeName getType() {
    return this.type;
  }

  /**
   * @return the field visibility to use.
   */
  public Visibility getFieldVisibility() {
    return Objects.requireNonNull(this.fieldVisibility);
  }

  /**
   * @return true if the field is final, or false if it is non-final.
   */
  public boolean isFinal() {
    return this.final_;
  }

  /**
   * @return true if the field is marked as transient, or false if non-transient.
   */
  public boolean isTransient() {
    return this.transient_;
  }

  /**
   * @return the accessor to override.
   */
  public ExecutableElement getGetterToOverride() {
    return this.getterToOverride;
  }

  /**
   * @return the deprecated annotation to add to generated logic, if provided.
   */
  public Optional<AnnotationMirror> getDeprecatedAnnotation() {
    return Optional.ofNullable(this.deprecatedAnnotation);
  }

  @Override
  public String toString() {
    return "Attribute{" +
        "identifier='" + this.identifier + "', " +
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
    private @MonotonicNonNull String identifier;
    private @MonotonicNonNull TypeName type;
    private @MonotonicNonNull Visibility fieldVisibility;
    private @MonotonicNonNull Boolean final_;
    private @MonotonicNonNull Boolean transient_;
    private @MonotonicNonNull ExecutableElement getterToOverride;
    private @MonotonicNonNull Boolean setterEnabled;
    private @MonotonicNonNull Boolean includeInToString;
    private @MonotonicNonNull Boolean includeInEqualsAndHashCode;

    // Nullable fields
    private @Nullable AnnotationMirror deprecatedAnnotation;

    private Builder() {
    }

    ExecutableElement getGetterToOverride() {
      return Objects.requireNonNull(this.getterToOverride);
    }

    String getName() {
      return Objects.requireNonNull(this.name);
    }

    public Builder name(String name) {
      this.name = Objects.requireNonNull(name);
      return this;
    }

    public Builder identifier(String identifier) {
      this.identifier = Objects.requireNonNull(identifier);
      return this;
    }

    public Builder type(TypeName type) {
      this.type = Objects.requireNonNull(type);
      return this;
    }

    public Builder fieldVisibility(Visibility fieldVisibility) {
      this.fieldVisibility = Objects.requireNonNull(fieldVisibility);
      return this;
    }

    public Builder final_(Boolean final_) {
      this.final_ = Objects.requireNonNull(final_);
      return this;
    }

    public Builder transient_(Boolean transient_) {
      this.transient_ = Objects.requireNonNull(transient_);
      return this;
    }

    public Builder getterToOverride(ExecutableElement getterToOverride) {
      this.getterToOverride = Objects.requireNonNull(getterToOverride);
      return this;
    }

    public Builder setterEnabled(Boolean setterEnabled) {
      this.setterEnabled = Objects.requireNonNull(setterEnabled);
      return this;
    }

    public Builder includeInToString(Boolean includeInToString) {
      this.includeInToString = Objects.requireNonNull(includeInToString);
      return this;
    }

    public Builder includeInEqualsAndHashCode(Boolean includeInEqualsAndHashCode) {
      this.includeInEqualsAndHashCode = Objects.requireNonNull(includeInEqualsAndHashCode);
      return this;
    }

    public Builder deprecatedAnnotation(@Nullable AnnotationMirror deprecatedAnnotation) {
      this.deprecatedAnnotation = deprecatedAnnotation;
      return this;
    }

    @Override
    public Attribute build() {
      return new Attribute(this);
    }
  }
}
