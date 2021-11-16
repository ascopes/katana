package io.ascopes.katana.ap.settings;

import java.util.Objects;
import java.util.function.Consumer;

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

  public String getName() {
    return this.name;
  }

  public Class<T> getType() {
    return this.type;
  }

  public T getInheritedValue() {
    return this.inheritedValue;
  }

  public T getImmutableDefaultValue() {
    return this.immutableDefaultValue;
  }

  public T getMutableDefaultValue() {
    return this.mutableDefaultValue;
  }

  public EqualityFunction<T> getEqualityCheck() {
    return this.equalityCheck;
  }

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
