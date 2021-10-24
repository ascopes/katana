package io.ascopes.katana.ap.settings;

import io.ascopes.katana.annotations.Settings;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.BiPredicate;

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
  private final Method method;
  private final BiPredicate<T, T> equalityCheck;

  public SettingSchema(String name, Class<T> type, Method method, BiPredicate<T, T> equalityCheck) {
    this.name = Objects.requireNonNull(name);
    this.type = Objects.requireNonNull(type);
    this.method = Objects.requireNonNull(method);
    this.equalityCheck = Objects.requireNonNull(equalityCheck);
  }

  public String getName() {
    return this.name;
  }

  public Class<T> getType() {
    return this.type;
  }

  @SuppressWarnings("unchecked")
  public T getInheritedValue() {
    return (T) this.method.getDefaultValue();
  }

  public boolean isInheritedValue(T value) {
    return this.equalityCheck.test(this.getInheritedValue(), value);
  }

  @SuppressWarnings("unchecked")
  public T getValue(Settings annotation) {
    try {
      return (T) this.method.invoke(annotation);
    } catch (Exception ex) {
      throw new IllegalArgumentException(
          "Failed to read default value on annotation method " + this.name,
          ex
      );
    }
  }
}
