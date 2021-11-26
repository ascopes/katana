package io.ascopes.katana.ap.codegen.components;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import io.ascopes.katana.ap.descriptors.Attribute;
import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import io.ascopes.katana.ap.utils.Logger;
import javax.lang.model.element.Modifier;

/**
 * Factory for building getters.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class GetterFactory {

  private final Logger logger;

  public GetterFactory() {
    this.logger = new Logger();
  }

  /**
   * Create an accessor method from a given attribute and the corresponding settings.
   *
   * @param attribute the attribute.
   * @param settings  the model settings.
   * @return the getter method.
   */
  public MethodSpec create(Attribute attribute, SettingsCollection settings) {
    MethodSpec.Builder builder = MethodSpec
        .overriding(attribute.getGetterToOverride())
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addJavadoc("@return the value of the {@code $L} attribute.", attribute.getName())
        .addStatement("return this.$L", attribute.getIdentifier());

    attribute
        .getDeprecatedAnnotation()
        .map(AnnotationSpec::get)
        .ifPresent(builder::addAnnotation);

    MethodSpec method = builder.build();
    this.logger.trace("Generated getter {}", method);
    return method;
  }
}
