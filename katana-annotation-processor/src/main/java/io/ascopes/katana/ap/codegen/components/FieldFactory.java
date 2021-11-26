package io.ascopes.katana.ap.codegen.components;

import com.squareup.javapoet.FieldSpec;
import io.ascopes.katana.ap.descriptors.Attribute;
import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import io.ascopes.katana.ap.utils.CodeGenUtils;
import io.ascopes.katana.ap.utils.Logger;
import javax.lang.model.element.Modifier;


/**
 * Factory for generating model fields.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class FieldFactory {

  private final Logger logger;

  public FieldFactory() {
    this.logger = new Logger();
  }

  public FieldSpec create(Attribute attribute, SettingsCollection settings) {
    FieldSpec.Builder builder = FieldSpec
        .builder(attribute.getType(), attribute.getIdentifier())
        .addModifiers(CodeGenUtils.modifiers(attribute.getFieldVisibility()));

    attribute
        .getDeprecatedAnnotation()
        .map(CodeGenUtils::copyDeprecatedFrom)
        .ifPresent(builder::addAnnotation);

    if (attribute.isFinal()) {
      builder.addModifiers(Modifier.FINAL);
    }

    if (attribute.isTransient()) {
      builder.addModifiers(Modifier.TRANSIENT);
    }

    FieldSpec field = builder.build();
    this.logger.trace("Generated field {}", field);
    return field;
  }
}
