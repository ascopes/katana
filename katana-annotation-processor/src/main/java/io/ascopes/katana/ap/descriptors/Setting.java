package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.annotations.Settings;
import java.util.Objects;
import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;


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
  private String name;
  private T value;
  private Class<T> valueType;
  private SettingLocation settingLocation;

  // These are null if the setting is a default setting, and non-null otherwise.
  private Element owningElement;
  private AnnotationMirror settingsAnnotation;
  private AnnotationValue settingsValue;

  /**
   * @return the name of the setting.
   */
  public String getName() {
    return Objects.requireNonNull(this.name);
  }

  /**
   * @param name the name to set.
   */
  public void setName(String name) {
    this.name = Objects.requireNonNull(name);
  }

  /**
   * @return the value of the setting.
   */
  public T getValue() {
    // Annotation values can't be null, so this is a perfectly valid assumption to be making.
    return Objects.requireNonNull(this.value);
  }

  /**
   * @param value the value to set.
   */
  public void setValue(T value) {
    this.value = Objects.requireNonNull(value);
  }

  /**
   * @return the type of the setting value.
   */
  public Class<T> getValueType() {
    return Objects.requireNonNull(this.valueType);
  }

  /**
   * @param valueType the value type to set.
   */
  public void setValueType(Class<T> valueType) {
    this.valueType = Objects.requireNonNull(valueType);
  }

  /**
   * @return where the setting came from.
   */
  public SettingLocation getSettingLocation() {
    return Objects.requireNonNull(this.settingLocation);
  }

  /**
   * @param settingLocation the settings location to set.
   */
  public void setSettingLocation(SettingLocation settingLocation) {
    this.settingLocation = Objects.requireNonNull(settingLocation);
  }

  /**
   * @return the element that owns the {@link Settings} annotation (null if not applicable).
   */
  public Optional<Element> getOwningElement() {
    return Optional.ofNullable(this.owningElement);
  }

  /**
   * @param owningElement the owning element to set, or null if one is not present.
   */
  public void setOwningElement(Element owningElement) {
    this.owningElement = owningElement;
  }

  /**
   * @return the {@link Settings} annotation (null if not applicable).
   */
  public Optional<AnnotationMirror> getSettingsAnnotation() {
    return Optional.ofNullable(this.settingsAnnotation);
  }

  /**
   * @param settingsAnnotation the settings annotation to set, or null if one is not present.
   */
  public void setSettingsAnnotation(AnnotationMirror settingsAnnotation) {
    this.settingsAnnotation = settingsAnnotation;
  }

  /**
   * @return the value in the {@link Settings} annotation (null if not applicable).
   */
  public Optional<AnnotationValue> getSettingsValue() {
    return Optional.ofNullable(this.settingsValue);
  }

  /**
   * @param settingsValue the settings value to set, or null if one is not present.
   */
  public void setSettingsValue(AnnotationValue settingsValue) {
    this.settingsValue = settingsValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "Setting{" +
        "name='" + this.name + '\'' +
        ", value=" + this.value +
        ", settingLocation=" + this.settingLocation +
        '}';
  }

}
