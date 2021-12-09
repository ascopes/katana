package io.ascopes.katana.ap.methods;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import io.ascopes.katana.ap.attributes.AttributeDescriptor;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import javax.lang.model.element.Modifier;

/**
 * Factory for building getters.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class GetterSourceFactory {

  private final Logger logger;

  /**
   * Initialize this factory.
   */
  public GetterSourceFactory() {
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  /**
   * Create a getter for the given attribute.
   *
   * @param attributeDescriptor the attribute to create the getter for.
   * @return the generated method spec.
   */
  public MethodSpec create(AttributeDescriptor attributeDescriptor) {
    MethodSpec.Builder builder = MethodSpec
        .overriding(attributeDescriptor.getGetterToOverride())
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addStatement("return this.$L", attributeDescriptor.getIdentifier());

    attributeDescriptor
        .getDeprecatedAnnotation()
        .map(AnnotationSpec::get)
        .ifPresent(builder::addAnnotation);

    MethodSpec method = builder.build();
    this.logger.trace("Generated getter\n{}", method);
    return method;
  }
}
