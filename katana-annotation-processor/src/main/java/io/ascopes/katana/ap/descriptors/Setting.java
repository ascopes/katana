package io.ascopes.katana.ap.descriptors;

import java.util.Objects;
import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;

public class Setting<T> implements Comparable<Setting<T>> {
  private final String name;
  private final T value;
  private final Class<? extends T> valueType;
  private final SettingLocation settingLocation;

  // These are null if the setting is a default setting, and non-null otherwise.
  private final Element owningElement;
  private final AnnotationMirror settingsAnnotation;
  private final AnnotationValue settingsValue;

  public Setting(
      String name,
      T value,
      Class<? extends T> valueType,
      SettingLocation settingLocation,
      Element owningElement,
      AnnotationMirror settingsAnnotation,
      AnnotationValue settingsValue
  ) {
    this.name = Objects.requireNonNull(name);
    this.value = Objects.requireNonNull(value);
    this.valueType = Objects.requireNonNull(valueType);
    this.settingLocation = settingLocation;

    this.settingsValue = settingsValue;
    this.owningElement = owningElement;
    this.settingsAnnotation = settingsAnnotation;
  }

  public String getName() {
    return this.name;
  }

  public T getValue() {
    return this.value;
  }

  public Class<? extends T> getValueType() {
    return this.valueType;
  }

  public SettingLocation getSettingLocation() {
    return this.settingLocation;
  }

  public Optional<Element> getOwningElement() {
    return Optional.ofNullable(this.owningElement);
  }

  public Optional<AnnotationMirror> getSettingsAnnotation() {
    return Optional.ofNullable(this.settingsAnnotation);
  }

  public Optional<AnnotationValue> getSettingsValue() {
    return Optional.ofNullable(this.settingsValue);
  }

  @Override
  public int compareTo(Setting<T> setting) {
    return this.settingLocation.compareTo(setting.settingLocation);
  }

  @Override
  public String toString() {
    return "Setting{" +
        "name='" + this.name + '\'' +
        ", value=" + this.value +
        ", settingLocation=" + this.settingLocation +
        '}';
  }

}
