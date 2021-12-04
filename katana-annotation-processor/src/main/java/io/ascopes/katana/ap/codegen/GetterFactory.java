package io.ascopes.katana.ap.codegen;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import io.ascopes.katana.ap.descriptors.Attribute;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import javax.lang.model.element.Modifier;

/**
 * Factory for building getters.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class GetterFactory {

  private final Logger logger;

  /**
   * Initialize this factory.
   */
  public GetterFactory() {
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  /**
   * Create a getter for the given attribute.
   *
   * @param attribute the attribute to create the getter for.
   * @return the generated method spec.
   */
  public MethodSpec create(Attribute attribute) {
    MethodSpec.Builder builder = MethodSpec
        .overriding(attribute.getGetterToOverride())
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addStatement("return this.$L", attribute.getIdentifier());

    attribute
        .getDeprecatedAnnotation()
        .map(AnnotationSpec::get)
        .ifPresent(builder::addAnnotation);

    MethodSpec method = builder.build();
    this.logger.trace("Generated getter\n{}", method);
    return method;
  }
}
