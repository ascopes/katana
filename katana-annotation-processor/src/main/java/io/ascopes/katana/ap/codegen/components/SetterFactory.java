package io.ascopes.katana.ap.codegen.components;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import io.ascopes.katana.ap.descriptors.Attribute;
import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import io.ascopes.katana.ap.utils.Logger;
import io.ascopes.katana.ap.utils.NamingUtils;
import javax.lang.model.element.Modifier;


/**
 * Factory for generating setters.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class SetterFactory {
  private final Logger logger;

  public SetterFactory() {
    this.logger = new Logger();
  }

  /**
   * Create a mutator for a given attribute and settings.
   *
   * @param attribute the attribute.
   * @param settings the settings for the model.
   * @return the setter method.
   */
  public MethodSpec create(Attribute attribute, SettingsCollection settings) {
    String setterName = NamingUtils.addPrefixCamelCase(
        settings.getSetterPrefix().getValue(),
        attribute.getName()
    );

    MethodSpec.Builder builder = MethodSpec
        .methodBuilder(setterName)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addParameter(attribute.getType(), attribute.getIdentifier(), Modifier.FINAL)
        .addJavadoc(
            "@param $L the value to set for the {@code $L} attribute",
            attribute.getIdentifier(),
            attribute.getName()
        )
        .addStatement("this.$1L = $1L", attribute.getIdentifier());

    attribute
        .getDeprecatedAnnotation()
        .map(AnnotationSpec::get)
        .ifPresent(builder::addAnnotation);

    MethodSpec method = builder.build();
    this.logger.trace("Generated setter method {}", method);
    return method;
  }
}
