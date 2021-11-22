package io.ascopes.katana.ap.settings;

import io.ascopes.katana.annotations.Settings;
import java.util.Objects;
import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;


/**
 * Descriptor for an individual setting value. A collection of these are expected to be able to
 * represent a hierarchy of inheritance, where settings specified on a more specific level can
 * override those on a less specific level.
 * <p>
 * An example of this is that a {@link Settings} annotation on an interface should take preference
 * over one specified on a package level. Likewise the package level should take preference over the
 * default framework settings.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class Setting<T> {

  private final SettingValueHolder<T> valueHolder;
  private final String description;
  private final SettingSchema<T> settingSchema;

  public Setting(
      SettingValueHolder<T> valueHolder,
      String description,
      SettingSchema<T> settingSchema
  ) {
    // Annotation values can't be null, so this is a perfectly valid assumption to be making.
    this.valueHolder = Objects.requireNonNull(valueHolder);
    this.description = Objects.requireNonNull(description);
    this.settingSchema = Objects.requireNonNull(settingSchema);
  }

  /**
   * @return the value of the setting.
   */
  public T getValue() {
    return this.valueHolder.getValue();
  }

  /**
   * @return the name of the location this setting was defined at.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * @return the element that the setting is declared on.
   */
  public Optional<Element> getDeclaringElement() {
    return this.valueHolder.getDeclaringElement();
  }


  /**
   * @return annotation mirror that the setting came from, if known. Otherwise, an empty optional.
   */
  public Optional<AnnotationMirror> getAnnotationMirror() {
    return this.valueHolder.getAnnotationMirror();
  }

  /**
   * @return annotation value that the setting came from, if known. Otherwise, an empty optional.
   */
  public Optional<AnnotationValue> getAnnotationValue() {
    return this.valueHolder.getAnnotationValue();
  }

  public Class<T> getType() {
    return this.settingSchema.getGenericType();
  }

  /**
   * @return the schema data for the setting.
   */
  SettingSchema<T> getSettingSchema() {
    return this.settingSchema;
  }
}
