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

  /**
   * Initialize the setting schema.
   *
   * @param name the name of the setting.
   * @param type the raw type of the setting.
   * @param immutableDefaultValue the default value for the setting for immutable models.
   * @param mutableDefaultValue the default value for the setting for mutable models.
   * @param builderSetter the setter on the builder to apply settings for this schema to.
   */
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

  /**
   * Get the setting name.
   * 
   * @return the name of the setting.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Get the setting raw type.
   *
   * @return the raw type of the setting.
   */
  public Class<?> getType() {
    return this.type;
  }

  /**
   * Get the raw type of the setting, cast to the actual parameterized type we want.
   *
   * @return the cast parameterized setting type. 
   */
  @SuppressWarnings("unchecked")
  public Class<T> getParameterizedTypeish() {
    // TODO: this is a massive hack, replace with a parameterized type or something?
    // This is not considered safe, and doesn't do what is expected if we were to consider this
    // on a deeper reflective level, but this bodge allows de-referencing arrays of generics
    // correctly while working around other JVM constraints, so I don't care too much.
    return (Class<T>) this.type;
  }

  /**
   * Get the default value for immutable models.
   * 
   * @return the default value for immutable models.
   */
  public T getImmutableDefaultValue() {
    return this.immutableDefaultValue;
  }

  /**
   * Get the default value for mutable models.
   *
   * @return the default value for mutable models.
   */
  public T getMutableDefaultValue() {
    return this.mutableDefaultValue;
  }

  /**
   * The setter to use for the {@link io.ascopes.katana.ap.settings.gen.SettingsCollection}
   * builder.
   *
   * @return the setter for this setting on the builder type.
   */
  public BuilderSetter<T> getBuilderSetter() {
    return this.builderSetter;
  }
}
