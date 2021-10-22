package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.annotations.EqualitySetting;
import io.ascopes.katana.annotations.Initializer;
import io.ascopes.katana.annotations.SetterSetting;
import io.ascopes.katana.annotations.ToStringSetting;
import java.util.Set;

public class SettingsCollection {
  private Setting<String> packageName;
  private Setting<Set<Initializer>> initializers;
  private Setting<EqualitySetting> equalitySetting;
  private Setting<ToStringSetting> stringificationSetting;
  private Setting<SetterSetting> setterSetting;

  public Setting<String> getPackageName() {
    return this.packageName;
  }

  public void setPackageName(Setting<String> packageName) {
    this.packageName = packageName;
  }

  public Setting<Set<Initializer>> getInitializers() {
    return this.initializers;
  }

  public void setInitializers(Setting<Set<Initializer>> initializers) {
    this.initializers = initializers;
  }

  public Setting<EqualitySetting> getEqualitySetting() {
    return this.equalitySetting;
  }

  public void setEqualitySetting(Setting<EqualitySetting> equalitySetting) {
    this.equalitySetting = equalitySetting;
  }

  public Setting<ToStringSetting> getStringificationSetting() {
    return this.stringificationSetting;
  }

  public void setStringificationSetting(Setting<ToStringSetting> stringificationSetting) {
    this.stringificationSetting = stringificationSetting;
  }

  public Setting<SetterSetting> getSetterSetting() {
    return this.setterSetting;
  }

  public void setSetterSetting(Setting<SetterSetting> setterSetting) {
    this.setterSetting = setterSetting;
  }
}
