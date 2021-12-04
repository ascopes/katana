package io.ascopes.katana.ap.codegen;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.ascopes.katana.ap.utils.CollectionUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


/**
 * A collection of members to apply to a type spec.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class TypeSpecMembers {

  private final List<MethodSpec> methods;
  private final List<FieldSpec> fields;
  private final List<TypeSpec> types;

  private TypeSpecMembers(Builder builder) {
    this.methods = CollectionUtils.freezeList(new ArrayList<>(builder.methods));
    this.fields = CollectionUtils.freezeList(new ArrayList<>(builder.fields));
    this.types = CollectionUtils.freezeList(new ArrayList<>(builder.types));
  }

  /**
   * Apply all generated members to the given typespec builder, adding them as members.
   *
   * @param typeSpecBuilder the builder to apply to.
   */
  public void applyTo(TypeSpec.Builder typeSpecBuilder) {
    this.methods.forEach(typeSpecBuilder::addMethod);
    this.fields.forEach(typeSpecBuilder::addField);
    this.types.forEach(typeSpecBuilder::addType);
  }

  /**
   * Initialize a new builder for this entity.
   *
   * @return the builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "TypeSpecMembers{"
        + "methods=" + this.methods + ", "
        + "fields=" + this.fields + ", "
        + "types=" + this.types
        + '}';
  }

  /**
   * Builder for a group of members.
   */
  public static final class Builder {

    private final List<MethodSpec> methods;
    private final List<FieldSpec> fields;
    private final List<TypeSpec> types;

    private Builder() {
      this.methods = new LinkedList<>();
      this.fields = new LinkedList<>();
      this.types = new LinkedList<>();
    }

    /**
     * Add a method spec.
     *
     * @param method the method spec to add.
     * @return this builder.
     */
    public Builder method(MethodSpec method) {
      this.methods.add(Objects.requireNonNull(method));
      return this;
    }

    /**
     * Add a field spec.
     *
     * @param field the field spec to add.
     * @return this builder.
     */
    public Builder field(FieldSpec field) {
      this.fields.add(Objects.requireNonNull(field));
      return this;
    }

    /**
     * Add a nested type spec.
     *
     * @param type the type spec to add.
     * @return this builder.
     */
    public Builder type(TypeSpec type) {
      this.types.add(Objects.requireNonNull(type));
      return this;
    }

    /**
     * Build a TypeSpecMembers instance from this builder.
     *
     * @return the generated entity.
     */
    public TypeSpecMembers build() {
      return new TypeSpecMembers(this);
    }
  }
}
