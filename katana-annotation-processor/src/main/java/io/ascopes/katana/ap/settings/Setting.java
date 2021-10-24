package io.ascopes.katana.ap.settings;

import io.ascopes.katana.annotations.Settings;
import java.util.Objects;


/**
 * Descriptor for an individual setting value. A collection of these are expected to be able
 * to represent a hierarchy of inheritance, where settings specified on a more specific level can
 * override those on a less specific level.
 * <p>
 * An example of this is that a {@link Settings} annotation on an interface should take preference
 * over one specified on a package level. Likewise the package level should take preference over
 * the default framework settings.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class Setting<T> {
  private final T value;
  private final SettingLocation settingLocation;
  private final SettingSchema<T> settingSchema;

  public Setting(T value, SettingLocation settingLocation, SettingSchema<T> settingSchema) {
    // Annotation values can't be null, so this is a perfectly valid assumption to be making.
    this.value = Objects.requireNonNull(value);
    this.settingLocation = Objects.requireNonNull(settingLocation);
    this.settingSchema = Objects.requireNonNull(settingSchema);
  }

  /**
   * @return the value of the setting.
   */
  public T getValue() {
    return this.value;
  }

  /**
   * @return where the setting came from.
   */
  public SettingLocation getSettingLocation() {
    return this.settingLocation;
  }

  /**
   * @return the schema data for the setting.
   */
  public SettingSchema<T> getSettingSchema() {
    return this.settingSchema;
  }
}
