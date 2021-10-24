package io.ascopes.katana.ap.settings;

import io.ascopes.katana.annotations.Settings;
import java.util.Objects;

/**
 * A single set of settings, from a specific location.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
final class RawSettings {
  private final String name;
  private final SettingLocation location;
  private final Settings value;

  public RawSettings(String name, SettingLocation location, Settings value) {
    this.name = Objects.requireNonNull(name);
    this.location = Objects.requireNonNull(location);
    this.value = Objects.requireNonNull(value);
  }

  public String getName() {
    return this.name;
  }

  public SettingLocation getLocation() {
    return this.location;
  }

  public Settings getValue() {
    return this.value;
  }
}
