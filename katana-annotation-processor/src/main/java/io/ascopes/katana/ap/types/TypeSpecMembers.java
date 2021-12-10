package io.ascopes.katana.ap.types;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.ascopes.katana.ap.utils.CollectionUtils;
import io.ascopes.katana.ap.utils.ObjectBuilder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.mustcall.qual.MustCall;


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

  private TypeSpecMembers(TypeSpecMembersBuilder typeSpecMembersBuilder) {
    this.methods = CollectionUtils.freezeList(new ArrayList<>(typeSpecMembersBuilder.methods));
    this.fields = CollectionUtils.freezeList(new ArrayList<>(typeSpecMembersBuilder.fields));
    this.types = CollectionUtils.freezeList(new ArrayList<>(typeSpecMembersBuilder.types));
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
  public static TypeSpecMembersBuilder builder() {
    return new TypeSpecMembersBuilder();
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
  @MustCall("build")
  public static final class TypeSpecMembersBuilder implements ObjectBuilder<TypeSpecMembers> {

    private final List<MethodSpec> methods;
    private final List<FieldSpec> fields;
    private final List<TypeSpec> types;

    private TypeSpecMembersBuilder() {
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
    public TypeSpecMembersBuilder method(MethodSpec method) {
      this.methods.add(Objects.requireNonNull(method));
      return this;
    }

    /**
     * Add a field spec.
     *
     * @param field the field spec to add.
     * @return this builder.
     */
    public TypeSpecMembersBuilder field(FieldSpec field) {
      this.fields.add(Objects.requireNonNull(field));
      return this;
    }

    /**
     * Add a nested type spec.
     *
     * @param type the type spec to add.
     * @return this builder.
     */
    public TypeSpecMembersBuilder type(TypeSpec type) {
      this.types.add(Objects.requireNonNull(type));
      return this;
    }

    /**
     * Build a TypeSpecMembers instance from this builder.
     *
     * @return the generated entity.
     */
    @Override
    public TypeSpecMembers build() {
      return new TypeSpecMembers(this);
    }
  }
}
