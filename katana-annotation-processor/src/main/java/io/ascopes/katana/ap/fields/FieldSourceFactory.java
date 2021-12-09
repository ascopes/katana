package io.ascopes.katana.ap.fields;

import com.squareup.javapoet.FieldSpec;
import io.ascopes.katana.ap.attributes.AttributeDescriptor;
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
public final class FieldSourceFactory {

  private final Logger logger;

  /**
   * Initialize the factory.
   */
  public FieldSourceFactory() {
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  /**
   * Create a field definition for a given attribute.
   *
   * @param attributeDescriptor the attribute to generate the field for.
   * @return the generated field spec.
   */
  public FieldSpec create(AttributeDescriptor attributeDescriptor) {
    FieldSpec.Builder builder = FieldSpec
        .builder(attributeDescriptor.getType(), attributeDescriptor.getIdentifier())
        .addModifiers(CodeGenUtils.modifiers(attributeDescriptor.getFieldVisibility()));

    attributeDescriptor
        .getDeprecatedAnnotation()
        .map(CodeGenUtils::copyDeprecatedFrom)
        .ifPresent(builder::addAnnotation);

    if (attributeDescriptor.isFinalField()) {
      builder.addModifiers(Modifier.FINAL);
    }

    if (attributeDescriptor.isTransientField()) {
      builder.addModifiers(Modifier.TRANSIENT);
    }

    FieldSpec field = builder.build();
    this.logger.trace("Generated field\n{}", field);
    return field;
  }
}
