package io.ascopes.katana.ap.settings;

import java.util.Objects;

/**
 * Schema for a setting entry. Defines the name of the setting, the value type, and the default
 * value that implies the setting is inherited.
 *
 * @param <T> the value type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class SettingSchema<T> {

  private final String name;

  // This explicitly is not extended from T, as it enables us to dereference arrays of generic
  // types correctly (e.g. an array of classes would be Class<?>[] but we can't produce a literal
  // for this as it is not reified, and Class<?>[].class is not valid syntax).
  private final Class<?> type;
  private final T immutableDefaultValue;
  private final T mutableDefaultValue;
  private final BuilderSetter<T> builderSetter;

  public SettingSchema(
      String name,
      Class<?> type,
      T immutableDefaultValue,
      T mutableDefaultValue,
      BuilderSetter<T> builderSetter
  ) {
    this.name = Objects.requireNonNull(name);
    this.type = Objects.requireNonNull(type);
    this.immutableDefaultValue = Objects.requireNonNull(immutableDefaultValue);
    this.mutableDefaultValue = Objects.requireNonNull(mutableDefaultValue);
    this.builderSetter = Objects.requireNonNull(builderSetter);
  }

  public String getName() {
    return this.name;
  }

  public Class<?> getType() {
    return this.type;
  }

  @SuppressWarnings("unchecked")
  public Class<T> getGenericType() {
    // This is not considered safe, and doesn't do what is expected if we were to consider this
    // on a deeper reflective level, but this bodge allows de-referencing arrays of generics
    // correctly while working around other JVM constraints, so I don't care too much.
    return (Class<T>) this.type;
  }

  public T getImmutableDefaultValue() {
    return this.immutableDefaultValue;
  }

  public T getMutableDefaultValue() {
    return this.mutableDefaultValue;
  }

  public BuilderSetter<T> getBuilderSetter() {
    return this.builderSetter;
  }
}
