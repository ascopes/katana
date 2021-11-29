package io.ascopes.katana.ap.codegen.components;

import com.squareup.javapoet.FieldSpec;
import io.ascopes.katana.ap.descriptors.Attribute;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.utils.CodeGenUtils;
import javax.lang.model.element.Modifier;


/**
 * Factory for generating model fields.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class FieldFactory {

  private final Logger logger;

  /**
   * Initialize the factory.
   */
  public FieldFactory() {
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  /**
   * Create a field definition for a given attribute.
   *
   * @param attribute the attribute to generate the field for.
   * @return the generated field spec.
   */
  public FieldSpec create(Attribute attribute) {
    FieldSpec.Builder builder = FieldSpec
        .builder(attribute.getType(), attribute.getIdentifier())
        .addModifiers(CodeGenUtils.modifiers(attribute.getFieldVisibility()));

    attribute
        .getDeprecatedAnnotation()
        .map(CodeGenUtils::copyDeprecatedFrom)
        .ifPresent(builder::addAnnotation);

    if (attribute.isFinalField()) {
      builder.addModifiers(Modifier.FINAL);
    }

    if (attribute.isTransientField()) {
      builder.addModifiers(Modifier.TRANSIENT);
    }

    FieldSpec field = builder.build();
    this.logger.trace("Generated field\n{}", field);
    return field;
  }
}
