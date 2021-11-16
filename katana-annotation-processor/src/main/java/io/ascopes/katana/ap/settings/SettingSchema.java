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
  private final Class<T> type;
  private final T inheritedValue;
  private final T immutableDefaultValue;
  private final T mutableDefaultValue;
  private final EqualityFunction<T> equalityCheck;
  private final BuilderSetter<T> builderSetter;

  /**
   * @param name                  the name of the setting.
   * @param type                  the type of the setting.
   * @param inheritedValue        the value to imply as inherited for the setting.
   * @param immutableDefaultValue the default value for immutable types for the setting.
   * @param mutableDefaultValue   the default value for mutable types for the setting.
   * @param equalityCheck         an equality function to determine if two instances of the setting
   *                              type are equal.
   * @param builderSetter         a method to set a setting derived from this schema on a
   *                              SettingsCollection builder.
   */
  public SettingSchema(
      String name,
      Class<T> type,
      T inheritedValue,
      T immutableDefaultValue,
      T mutableDefaultValue,
      EqualityFunction<T> equalityCheck,
      BuilderSetter<T> builderSetter
  ) {
    this.name = Objects.requireNonNull(name);
    this.type = Objects.requireNonNull(type);
    this.inheritedValue = Objects.requireNonNull(inheritedValue);
    this.immutableDefaultValue = Objects.requireNonNull(immutableDefaultValue);
    this.mutableDefaultValue = Objects.requireNonNull(mutableDefaultValue);
    this.equalityCheck = Objects.requireNonNull(equalityCheck);
    this.builderSetter = Objects.requireNonNull(builderSetter);
  }

  /**
   * @return the setting name.
   */
  public String getName() {
    return this.name;
  }

  /**
   * @return the setting type.
   */
  public Class<T> getType() {
    return this.type;
  }

  /**
   * @return the value to imply as inherited on the setting.
   */
  public T getInheritedValue() {
    return this.inheritedValue;
  }

  /**
   * @return the default value for immutable types for the setting.
   */
  public T getImmutableDefaultValue() {
    return this.immutableDefaultValue;
  }

  /**
   * @return the default value for mutable types for the setting.
   */
  public T getMutableDefaultValue() {
    return this.mutableDefaultValue;
  }

  /**
   * @return an equality function to determine if two instances of the setting type are equal.
   */
  public EqualityFunction<T> getEqualityCheck() {
    return this.equalityCheck;
  }

  /**
   * @return a method to set a setting derived from this schema on a SettingsCollection builder.
   */
  public BuilderSetter<T> getBuilderSetter() {
    return this.builderSetter;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "SettingSchema{" +
        "name='" + this.name + '\'' +
        ", type=" + this.type +
        '}';
  }
}
