package io.ascopes.katana.ap.codegen.components;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import io.ascopes.katana.ap.descriptors.Attribute;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.settings.gen.SettingsCollection;
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
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  public MethodSpec create(Attribute attribute, SettingsCollection settings) {
    String setterName = NamingUtils.addPrefixCamelCase(
        settings.getSetterPrefix().getValue(),
        attribute.getName()
    );

    MethodSpec.Builder builder = MethodSpec
        .methodBuilder(setterName)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addParameter(attribute.getType(), attribute.getIdentifier(), Modifier.FINAL)
        .addStatement("this.$1L = $1L", attribute.getIdentifier());

    attribute
        .getDeprecatedAnnotation()
        .map(AnnotationSpec::get)
        .ifPresent(builder::addAnnotation);

    MethodSpec method = builder.build();
    this.logger.trace("Generated setter\n{}", method);
    return method;
  }
}
