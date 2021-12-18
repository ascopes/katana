/*
 * Copyright (C) 2021 Ashley Scopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ascopes.katana.ap.types;

import com.squareup.javapoet.ClassName;
import io.ascopes.katana.ap.attributes.AttributeDescriptor;
import io.ascopes.katana.ap.builders.BuilderStrategyDescriptor;
import io.ascopes.katana.ap.methods.EqualityStrategyDescriptor;
import io.ascopes.katana.ap.methods.ToStringStrategyDescriptor;
import io.ascopes.katana.ap.utils.CollectionUtils;
import io.ascopes.katana.ap.utils.ObjectBuilder;
import io.ascopes.katana.ap.utils.StringUtils;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.checkerframework.checker.mustcall.qual.MustCall;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.optional.qual.MaybePresent;

/**
 * Descriptor for a model that should be generated.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class ModelDescriptor {

  private final String packageName;
  private final String className;
  private final TypeElement superInterface;
  private final Set<Constructor> constructors;
  private final SortedSet<AttributeDescriptor> attributeDescriptors;
  private final String indent;
  private final String setterPrefix;

  // Nullable attributes
  private final @Nullable BuilderStrategyDescriptor builderStrategyDescriptor;
  private final @Nullable EqualityStrategyDescriptor equalityStrategy;
  private final @Nullable ToStringStrategyDescriptor toStringStrategy;
  private final @Nullable AnnotationMirror deprecatedAnnotation;

  private ModelDescriptor(ModelDescriptorBuilder builder) {
    this.packageName = Objects.requireNonNull(builder.packageName);
    this.className = Objects.requireNonNull(builder.className);
    this.superInterface = Objects.requireNonNull(builder.superInterface);
    this.indent = Objects.requireNonNull(builder.indent);
    this.setterPrefix = Objects.requireNonNull(builder.setterPrefix);

    // Collection attributes.
    this.constructors = CollectionUtils.freezeSet(builder.constructors);
    this.attributeDescriptors = CollectionUtils.freezeSortedSet(builder.attributeDescriptors);

    // Nullable attributes.
    this.builderStrategyDescriptor = builder.builderStrategyDescriptor;
    this.equalityStrategy = builder.equalityStrategy;
    this.toStringStrategy = builder.toStringStrategy;
    this.deprecatedAnnotation = builder.deprecatedAnnotation;
  }

  /**
   * Get the package name for the generated model.
   *
   * @return the package name for the generated model.
   */
  public String getPackageName() {
    return this.packageName;
  }

  /**
   * Get the class name for the generated model.
   *
   * @return the class name for the generated model.
   */
  public String getClassName() {
    return this.className;
  }

  /**
   * Get the qualified name for the generated model.
   *
   * @return the qualified name for the generated model.
   */
  public ClassName getQualifiedName() {
    return ClassName.get(this.packageName, this.className);
  }

  /**
   * Get the super-interface that defined this model.
   *
   * @return the super-interface that defined this model.
   */
  public TypeElement getSuperInterface() {
    return this.superInterface;
  }

  /**
   * Get the set of constructors to use for this model.
   *
   * @return the set of constructors.
   */
  public Set<Constructor> getConstructors() {
    return this.constructors;
  }

  /**
   * Get the attributes declared on this model.
   *
   * @return the attributes declared on this model.
   */
  public SortedSet<AttributeDescriptor> getAttributes() {
    return this.attributeDescriptors;
  }

  /**
   * Get the indent for the generated code.
   *
   * @return the indent for the generated code.
   */
  public String getIndent() {
    return this.indent;
  }

  /**
   * Get the setter prefix for the generated code.
   *
   * @return the setter prefix for the generated code.
   */
  public String getSetterPrefix() {
    return this.setterPrefix;
  }

  /**
   * Get the builder strategy to use to generate a builder, if one is enabled.
   *
   * @return the builder strategy, or an empty optional if not enabled.
   */
  @MaybePresent
  public Optional<BuilderStrategyDescriptor> getBuilderStrategy() {
    return Optional.ofNullable(this.builderStrategyDescriptor);
  }

  /**
   * Get the equality strategy to use to generate an equals and hashcode method, if one is enabled.
   *
   * @return the equality strategy, or an empty optional if not enabled.
   */
  @MaybePresent
  public Optional<EqualityStrategyDescriptor> getEqualityStrategy() {
    return Optional.ofNullable(this.equalityStrategy);
  }

  /**
   * Get the strategy to use to generate toString methods.
   *
   * @return the toString strategy, or an empty optional if not enabled.
   */
  @MaybePresent
  public Optional<ToStringStrategyDescriptor> getToStringStrategy() {
    return Optional.ofNullable(this.toStringStrategy);
  }

  /**
   * Get the deprecated annotation to apply to the generated model type, if one is provided on the
   * super-interface.
   *
   * @return the deprecated annotation, or an empty optional if not provided.
   */
  @MaybePresent
  public Optional<? extends AnnotationMirror> getDeprecatedAnnotation() {
    return Optional.ofNullable(this.deprecatedAnnotation);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "ModelDescriptor{"
        + "qualifiedName=" + StringUtils.quoted(this.getQualifiedName()) + ", "
        + "superInterface=" + StringUtils.quoted(this.superInterface.getQualifiedName()) + ", "
        + "constructors=" + this.constructors + ", "
        + "attributes=" + this.attributeDescriptors + ", "
        + "indent=" + StringUtils.quoted(this.indent) + ", "
        + "setterPrefix=" + StringUtils.quoted(this.setterPrefix) + ", "
        + "builderStrategy=" + this.builderStrategyDescriptor + ", "
        + "equalityStrategy=" + this.equalityStrategy + ", "
        + "toStringStrategy=" + this.toStringStrategy + ", "
        + "deprecatedAnnotation=" + this.deprecatedAnnotation
        + '}';
  }

  /**
   * Create a builder for a new model.
   *
   * @return the builder.
   */
  @MustCall("build")
  public static ModelDescriptorBuilder builder() {
    return new ModelDescriptorBuilder();
  }

  /**
   * Builder for Model objects.
   */
  @SuppressWarnings("UnusedReturnValue")
  @MustCall("build")
  public static final class ModelDescriptorBuilder implements ObjectBuilder<ModelDescriptor> {

    private final Set<Constructor> constructors;
    private final SortedSet<AttributeDescriptor> attributeDescriptors;

    private @MonotonicNonNull String packageName;
    private @MonotonicNonNull String className;
    private @MonotonicNonNull TypeElement superInterface;
    private @MonotonicNonNull String indent;
    private @MonotonicNonNull String setterPrefix;

    // Nullable attributes
    private @Nullable BuilderStrategyDescriptor builderStrategyDescriptor;
    private @Nullable EqualityStrategyDescriptor equalityStrategy;
    private @Nullable ToStringStrategyDescriptor toStringStrategy;
    private @Nullable AnnotationMirror deprecatedAnnotation;

    private ModelDescriptorBuilder() {
      this.constructors = new HashSet<>();
      this.attributeDescriptors = new TreeSet<>(Comparator.comparing(AttributeDescriptor::getName));
    }

    /**
     * Set the package name.
     *
     * <p>Empty packages are denoted by an empty string {@code ""}.
     *
     * @param packageName the package name to set.
     * @return this builder.
     */
    public ModelDescriptorBuilder packageName(String packageName) {
      this.packageName = Objects.requireNonNull(packageName);
      return this;
    }

    /**
     * Set the class name.
     *
     * @param className the class name to set.
     * @return this builder.
     */
    public ModelDescriptorBuilder className(String className) {
      this.className = Objects.requireNonNull(className);
      return this;
    }

    /**
     * Set the super-interface that defined this model.
     *
     * @param superInterface the super-interface.
     * @return this builder.
     */
    public ModelDescriptorBuilder superInterface(TypeElement superInterface) {
      this.superInterface = Objects.requireNonNull(superInterface);
      return this;
    }

    /**
     * Add an attribute.
     *
     * @param attributeDescriptor the attribute to add.
     * @return this builder.
     */
    public ModelDescriptorBuilder attribute(AttributeDescriptor attributeDescriptor) {
      Objects.requireNonNull(attributeDescriptor);
      this.attributeDescriptors.add(attributeDescriptor);
      return this;
    }

    /**
     * Add a constructor.
     *
     * @param constructor the constructor to add.
     * @return this builder.
     */
    public ModelDescriptorBuilder constructor(Constructor constructor) {
      Objects.requireNonNull(constructor);
      this.constructors.add(constructor);
      return this;
    }

    /**
     * Set the indent for output code.
     *
     * @param indent the indent to use.
     * @return this builder.
     */
    public ModelDescriptorBuilder indent(String indent) {
      this.indent = Objects.requireNonNull(indent);
      return this;
    }

    /**
     * Set the setter prefix to use.
     *
     * @param setterPrefix the setter prefix to use.
     * @return this builder.
     */
    public ModelDescriptorBuilder setterPrefix(String setterPrefix) {
      this.setterPrefix = Objects.requireNonNull(setterPrefix);
      return this;
    }

    /**
     * Set the builder strategy.
     *
     * @param builderStrategyDescriptor the nullable builder strategy to set.
     * @return this builder.
     */
    public ModelDescriptorBuilder builderStrategy(
        @Nullable BuilderStrategyDescriptor builderStrategyDescriptor) {
      this.builderStrategyDescriptor = builderStrategyDescriptor;
      return this;
    }

    /**
     * Set the equality strategy.
     *
     * @param equalityStrategy the nullable equality strategy to set.
     * @return this builder.
     */
    public ModelDescriptorBuilder equalityStrategy(
        @Nullable EqualityStrategyDescriptor equalityStrategy) {
      this.equalityStrategy = equalityStrategy;
      return this;
    }

    /**
     * Set the toString strategy.
     *
     * @param toStringStrategy the nullable toString strategy to set.
     * @return this builder.
     */
    public ModelDescriptorBuilder toStringStrategy(
        @Nullable ToStringStrategyDescriptor toStringStrategy) {
      this.toStringStrategy = toStringStrategy;
      return this;
    }

    /**
     * Set the model-level deprecation annotation.
     *
     * @param deprecatedAnnotation the nullable annotation mirror to set.
     * @return this builder.
     */
    public ModelDescriptorBuilder deprecatedAnnotation(
        @Nullable AnnotationMirror deprecatedAnnotation
    ) {
      this.deprecatedAnnotation = deprecatedAnnotation;
      return this;
    }

    /**
     * Build a Model instance from this builder.
     *
     * @return the constructed Model object.
     */
    @Override
    public ModelDescriptor build() {
      return new ModelDescriptor(this);
    }
  }
}
