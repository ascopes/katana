package io.ascopes.katana.ap.descriptors;

import com.squareup.javapoet.TypeName;
import io.ascopes.katana.annotations.Visibility;
import io.ascopes.katana.ap.utils.ObjectBuilder;
import io.ascopes.katana.ap.utils.StringUtils;
import java.util.Objects;
import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import org.checkerframework.checker.mustcall.qual.MustCall;
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
  private final boolean finalField;
  private final boolean transientField;
  private final ExecutableElement getterToOverride;
  private final boolean setterEnabled;
  private final boolean includeInToString;
  private final boolean includeInEqualsAndHashCode;

  // Nullable fields
  private final @Nullable AnnotationMirror deprecatedAnnotation;

  private Attribute(AttributeBuilder builder) {
    this.name = Objects.requireNonNull(builder.name);
    this.identifier = Objects.requireNonNull(builder.identifier);
    this.type = Objects.requireNonNull(builder.type);
    this.fieldVisibility = Objects.requireNonNull(builder.fieldVisibility);
    this.finalField = Objects.requireNonNull(builder.finalField);
    this.transientField = Objects.requireNonNull(builder.transientField);
    this.getterToOverride = Objects.requireNonNull(builder.getter);
    this.setterEnabled = Objects.requireNonNull(builder.setterEnabled);
    this.includeInToString = Objects.requireNonNull(builder.includeInToString);
    this.includeInEqualsAndHashCode = Objects.requireNonNull(builder.includeInEqualsAndHashCode);

    // Nullable fields
    this.deprecatedAnnotation = builder.deprecatedAnnotation;
  }

  /**
   * Get the attribute name.
   *
   * @return the name of the attribute.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Get the identifier name for the attribute.
   *
   * @return the identifier for the attribute.
   */
  public String getIdentifier() {
    return this.identifier;
  }

  /**
   * Get the name of the type for the attribute.
   *
   * @return the attribute type name.
   */
  public TypeName getType() {
    return this.type;
  }

  /**
   * Get the visibility for the field.
   *
   * @return the visibility of the field.
   */
  public Visibility getFieldVisibility() {
    return Objects.requireNonNull(this.fieldVisibility);
  }

  /**
   * Determine whether the field is marked as final or not.
   *
   * @return true if final, false otherwise.
   */
  public boolean isFinalField() {
    return this.finalField;
  }

  /**
   * Determine whether the field is marked as transient or not.
   *
   * @return true if transient, false otherwise.
   */
  public boolean isTransientField() {
    return this.transientField;
  }

  /**
   * Get the getter method to override.
   *
   * @return the getter to override.
   */
  public ExecutableElement getGetterToOverride() {
    return this.getterToOverride;
  }

  /**
   * Determine whether this attribute should be included in the generated equals and hashcode
   * methods.
   *
   * <p>This is meaningless if generation is disabled or set to use a custom method.
   *
   * @return true if included, false if excluded.
   */
  public boolean getIncludeInEqualsAndHashCode() {
    return this.includeInEqualsAndHashCode;
  }

  /**
   * Determine whether this attribute should be included in the generated toString method.
   *
   * <p>This is meaningless if generation is disabled or set to use a custom method.
   *
   * @return true if included, false if excluded.
   */
  public boolean getIncludeInToStrnig() {
    return this.includeInToString;
  }

  /**
   * Get the deprecated annotation from the overridden getter, if it is present.
   *
   * @return the deprecated annotation, or an empty optional if not present.
   */
  public Optional<AnnotationMirror> getDeprecatedAnnotation() {
    return Optional.ofNullable(this.deprecatedAnnotation);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "Attribute{"
        + "identifier=" + StringUtils.quoted(this.identifier) + ", "
        + "type=" + StringUtils.quoted(this.getterToOverride.getReturnType()) + ", "
        + "get=true, "
        + "set=" + this.setterEnabled
        + '}';
  }

  /**
   * Create a builder for a new Attribute.
   *
   * @return the builder.
   */
  @MustCall("build")
  public static AttributeBuilder builder() {
    return new AttributeBuilder();
  }

  /**
   * The builder for an attribute.
   */
  @SuppressWarnings("UnusedReturnValue")
  @MustCall("build")
  public static final class AttributeBuilder implements ObjectBuilder<Attribute> {

    private @MonotonicNonNull String name;
    private @MonotonicNonNull String identifier;
    private @MonotonicNonNull TypeName type;
    private @MonotonicNonNull Visibility fieldVisibility;
    private @MonotonicNonNull Boolean finalField;
    private @MonotonicNonNull Boolean transientField;
    private @MonotonicNonNull ExecutableElement getter;
    private @MonotonicNonNull Boolean setterEnabled;
    private @MonotonicNonNull Boolean includeInToString;
    private @MonotonicNonNull Boolean includeInEqualsAndHashCode;

    // Nullable fields
    private @Nullable AnnotationMirror deprecatedAnnotation;

    private AttributeBuilder() {
    }

    /**
     * Set the getter to override.
     *
     * @param getter the getter to override.
     * @return this builder.
     */
    public AttributeBuilder getter(ExecutableElement getter) {
      this.getter = Objects.requireNonNull(getter);
      return this;
    }

    /**
     * Set the name of the attribute.
     *
     * @param name the name of the attribute.
     * @return this builder.
     */
    public AttributeBuilder name(String name) {
      this.name = Objects.requireNonNull(name);
      return this;
    }

    /**
     * Set the identifier for the attribute field.
     *
     * @param identifier the identifier for the attribute field.
     * @return this builder.
     */
    public AttributeBuilder identifier(String identifier) {
      this.identifier = Objects.requireNonNull(identifier);
      return this;
    }

    /**
     * Set the type name of the attribute.
     *
     * @param type the type name for the attribute.
     * @return this builder.
     */
    public AttributeBuilder type(TypeName type) {
      this.type = Objects.requireNonNull(type);
      return this;
    }

    /**
     * Set the visibility for the field.
     *
     * @param fieldVisibility the visibility for the field.
     * @return this builder.
     */
    public AttributeBuilder fieldVisibility(Visibility fieldVisibility) {
      this.fieldVisibility = Objects.requireNonNull(fieldVisibility);
      return this;
    }

    /**
     * Set whether this field is final or not.
     *
     * @param finalField whether the field is final or not.
     * @return this builder.
     */
    public AttributeBuilder finalField(Boolean finalField) {
      this.finalField = Objects.requireNonNull(finalField);
      return this;
    }

    /**
     * Set whether this field is transient or not.
     *
     * @param transientField whether the field is transient or not.
     * @return this builder.
     */
    public AttributeBuilder transientField(Boolean transientField) {
      this.transientField = Objects.requireNonNull(transientField);
      return this;
    }

    /**
     * Set whether this attribute has a setter or not.
     *
     * @param setterEnabled whether the field has a setter or not.
     * @return this builder.
     */
    public AttributeBuilder setterEnabled(Boolean setterEnabled) {
      this.setterEnabled = Objects.requireNonNull(setterEnabled);
      return this;
    }

    /**
     * Set whether this attribute is included in a generated toString override or not.
     *
     * @param includeInToString whether this attribute is included in a toString or not.
     * @return this builder.
     */
    public AttributeBuilder includeInToString(Boolean includeInToString) {
      this.includeInToString = Objects.requireNonNull(includeInToString);
      return this;
    }

    /**
     * Set whether this attribute is included in a generated equals and hashCode override or not.
     *
     * @param includeInEqualsAndHashCode whether this attribute is included in an equals and
     *                                   hashCode or not.
     * @return this builder.
     */
    public AttributeBuilder includeInEqualsAndHashCode(Boolean includeInEqualsAndHashCode) {
      this.includeInEqualsAndHashCode = Objects.requireNonNull(includeInEqualsAndHashCode);
      return this;
    }

    /**
     * Set the deprecated annotation that was applied to the getter.
     *
     * @param deprecatedAnnotation the nullable deprecated annotation.
     * @return this builder.
     */
    public AttributeBuilder deprecatedAnnotation(@Nullable AnnotationMirror deprecatedAnnotation) {
      this.deprecatedAnnotation = deprecatedAnnotation;
      return this;
    }

    /**
     * Build a new attribute from this builder.
     *
     * @return the generated attribute.
     */
    @Override
    public Attribute build() {
      return new Attribute(this);
    }
  }
}
