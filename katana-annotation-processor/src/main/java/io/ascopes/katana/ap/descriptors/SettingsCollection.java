package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.annotations.EqualitySetting;
import io.ascopes.katana.annotations.Initializer;
import io.ascopes.katana.annotations.SetterSetting;
import io.ascopes.katana.annotations.Settings;
import io.ascopes.katana.annotations.ToStringSetting;
import java.util.Objects;
import java.util.Set;

/**
 * A collection of settings. Each of the fields here should correspond to one in
 * {@link Settings}, possibly with some form of manipulation expected first.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class SettingsCollection {
  private Setting<String> packageName;
  private Setting<Set<Initializer>> initializers;
  private Setting<EqualitySetting> equalitySetting;
  private Setting<ToStringSetting> stringificationSetting;
  private Setting<SetterSetting> setterSetting;

  /**
   * @return the package name setting.
   */
  public Setting<String> getPackageName() {
    return Objects.requireNonNull(this.packageName);
  }

  /**
   * @param packageName the package name setting to set.
   */
  public void setPackageName(Setting<String> packageName) {
    this.packageName = Objects.requireNonNull(packageName);
  }

  /**
   * @return the initializer settings.
   */
  public Setting<Set<Initializer>> getInitializers() {
    return this.initializers;
  }

  /**
   * @param initializers the initializer settings to set.
   */
  public void setInitializers(Setting<Set<Initializer>> initializers) {
    this.initializers = Objects.requireNonNull(initializers);
  }

  /**
   * @return the equality settings.
   */
  public Setting<EqualitySetting> getEqualitySetting() {
    return Objects.requireNonNull(this.equalitySetting);
  }

  /**
   * @param equalitySetting the equality settings to set.
   */
  public void setEqualitySetting(Setting<EqualitySetting> equalitySetting) {
    this.equalitySetting = equalitySetting;
  }

  /**
   * @return the stringification settings.
   */
  public Setting<ToStringSetting> getStringificationSetting() {
    return Objects.requireNonNull(this.stringificationSetting);
  }

  /**
   * @param stringificationSetting the stringification settings to set.
   */
  public void setStringificationSetting(Setting<ToStringSetting> stringificationSetting) {
    this.stringificationSetting = Objects.requireNonNull(stringificationSetting);
  }

  /**
   * @return the setter settings.
   */
  public Setting<SetterSetting> getSetterSetting() {
    return Objects.requireNonNull(this.setterSetting);
  }

  /**
   * I am aware that this method reads absolutely awfully. If you can think of a nice replacement,
   * I will buy you a beer.
   *
   * @param setterSetting the setter settings to set.
   */
  public void setSetterSetting(Setting<SetterSetting> setterSetting) {
    this.setterSetting = Objects.requireNonNull(setterSetting);
  }
}
