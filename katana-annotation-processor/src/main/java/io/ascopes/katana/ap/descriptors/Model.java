package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.settings.gen.SettingsCollection;
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
public final class Model {

  private final String packageName;
  private final String className;
  private final String qualifiedName;
  private final boolean mutable;
  private final TypeElement superInterface;
  private final AnnotationMirror annotationMirror;
  private final SettingsCollection settingsCollection;
  private final MethodClassification methods;
  private final Set<Constructor> constructors;
  private final SortedSet<Attribute> attributes;

  // Nullable attributes
  private final @Nullable BuilderStrategy builderStrategy;
  private final @Nullable AnnotationMirror deprecatedAnnotation;

  private Model(Builder builder) {
    this.packageName = Objects.requireNonNull(builder.packageName);
    this.className = Objects.requireNonNull(builder.className);
    this.qualifiedName = Objects.requireNonNull(builder.qualifiedName);
    this.mutable = Objects.requireNonNull(builder.mutable);
    this.superInterface = Objects.requireNonNull(builder.superInterface);
    this.annotationMirror = Objects.requireNonNull(builder.annotationMirror);
    this.settingsCollection = Objects.requireNonNull(builder.settingsCollection);
    this.methods = Objects.requireNonNull(builder.methods);

    this.constructors = CollectionUtils.freeze(builder.constructors);
    this.attributes = CollectionUtils.freeze(builder.attributes);

    // Nullable attributes
    this.builderStrategy = builder.builderStrategy;
    this.deprecatedAnnotation = builder.deprecatedAnnotation;
  }

  public String getPackageName() {
    return this.packageName;
  }

  public String getClassName() {
    return this.className;
  }

  public String getQualifiedName() {
    return this.qualifiedName;
  }

  public boolean isMutable() {
    return this.mutable;
  }

  public TypeElement getSuperInterface() {
    return this.superInterface;
  }

  public SettingsCollection getSettingsCollection() {
    return this.settingsCollection;
  }

  public Set<Constructor> getConstructors() {
    return this.constructors;
  }

  public SortedSet<Attribute> getAttributes() {
    return this.attributes;
  }

  @MaybePresent
  public Optional<BuilderStrategy> getBuilderStrategy() {
    return Optional.ofNullable(this.builderStrategy);
  }

  @MaybePresent
  public Optional<AnnotationMirror> getDeprecatedAnnotation() {
    return Optional.ofNullable(this.deprecatedAnnotation);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "Model{" +
        "packageName=" + StringUtils.quoted(this.packageName) + ", " +
        "className=" + StringUtils.quoted(this.className) + ", " +
        "attributes=" + this.attributes + ", " +
        "superInterface=" + StringUtils.quoted(this.superInterface.getQualifiedName()) + ", " +
        "mutable=" + this.mutable +
        '}';
  }

  @MustCall("build")
  public static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("UnusedReturnValue")
  @MustCall("build")
  public static final class Builder implements ObjectBuilder<Model> {

    private final Set<Constructor> constructors;
    private final SortedSet<Attribute> attributes;

    private @MonotonicNonNull String packageName;
    private @MonotonicNonNull String className;
    private @MonotonicNonNull String qualifiedName;
    private @MonotonicNonNull TypeElement superInterface;
    private @MonotonicNonNull Boolean mutable;
    private @MonotonicNonNull AnnotationMirror annotationMirror;
    private @MonotonicNonNull SettingsCollection settingsCollection;
    private @MonotonicNonNull MethodClassification methods;

    // Nullable attributes
    private @Nullable BuilderStrategy builderStrategy;
    private @Nullable AnnotationMirror deprecatedAnnotation;

    private Builder() {
      this.constructors = new HashSet<>();
      this.attributes = new TreeSet<>(Comparator.comparing(Attribute::getName));
    }

    TypeElement getSuperInterface() {
      return Objects.requireNonNull(this.superInterface);
    }

    boolean isMutable() {
      return Objects.requireNonNull(this.mutable);
    }

    AnnotationMirror getAnnotationMirror() {
      return Objects.requireNonNull(this.annotationMirror);
    }

    SettingsCollection getSettingsCollection() {
      return Objects.requireNonNull(this.settingsCollection);
    }

    MethodClassification getMethods() {
      return Objects.requireNonNull(this.methods);
    }

    public Builder attributes(Set<Attribute> attributes) {
      Objects.requireNonNull(attributes, "attribute set was null");
      for (Attribute attribute : attributes) {
        this.attributes.add(Objects.requireNonNull(attribute, "attribute was null"));
      }
      return this;
    }

    public Builder constructors(Set<Constructor> constructors) {
      Objects.requireNonNull(constructors, "constructors set was null");
      for (Constructor constructor : constructors) {
        this.constructors.add(Objects.requireNonNull(constructor, "constructor was null"));
      }
      return this;
    }

    public Builder packageName(String packageName) {
      this.packageName = packageName;
      return this;
    }

    public Builder className(String className) {
      this.className = Objects.requireNonNull(className);
      return this;
    }

    public Builder qualifiedName(String qualifiedName) {
      this.qualifiedName = Objects.requireNonNull(qualifiedName);
      return this;
    }

    public Builder mutable(Boolean mutable) {
      // Box to enable us to error later if it was not explicitly set.
      this.mutable = Objects.requireNonNull(mutable);
      return this;
    }

    public Builder superInterface(TypeElement superInterface) {
      this.superInterface = Objects.requireNonNull(superInterface);
      return this;
    }

    public Builder annotationMirror(AnnotationMirror annotationMirror) {
      this.annotationMirror = Objects.requireNonNull(annotationMirror);
      return this;
    }

    public Builder settingsCollection(SettingsCollection settingsCollection) {
      this.settingsCollection = settingsCollection;
      return this;
    }

    public Builder methods(MethodClassification methods) {
      this.methods = methods;
      return this;
    }

    public Builder builderStrategy(@Nullable BuilderStrategy builderStrategy) {
      this.builderStrategy = builderStrategy;
      return this;
    }

    public Builder deprecatedAnnotation(@Nullable AnnotationMirror deprecatedAnnotation) {
      this.deprecatedAnnotation = deprecatedAnnotation;
      return this;
    }

    @Override
    public Model build() {
      return new Model(this);
    }
  }
}
