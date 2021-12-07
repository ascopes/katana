package io.ascopes.katana.ap.codegen;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import io.ascopes.katana.ap.analysis.Attribute;
import io.ascopes.katana.ap.analysis.Constructor;
import io.ascopes.katana.ap.analysis.Model;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;

/**
 * Factory for creating general purpose public constructors.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
final class ConstructorFactory {

  private final Logger logger;

  /**
   * Initialize this factory.
   */
  ConstructorFactory() {
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  /**
   * Create constructors for the given model.
   *
   * <p>This will not include constructors generated for builder integration.
   *
   * @param model the model to use.
   * @return a stream of generated method specs.
   */
  Stream<MethodSpec> create(Model model) {
    return model
        .getConstructors()
        .stream()
        .map(constructor -> this.createConstructor(model, constructor));
  }

  private MethodSpec createConstructor(Model model, Constructor constructor) {
    switch (constructor) {
      case COPY:
        return this.createCopyConstructor(model);
      case NO_ARGS:
        return this.createNoArgsConstructor(model);
      case ALL_ARGS:
        return this.createAllArgsConstructor(model);
      default:
        throw new UnsupportedOperationException("Unknown constructor type " + constructor);
    }
  }

  private MethodSpec createCopyConstructor(Model model) {
    TypeName interfaceType = TypeName.get(model.getSuperInterface().asType());

    MethodSpec.Builder methodBuilder = MethodSpec
        .constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(interfaceType, "model", Modifier.FINAL);

    for (Attribute attribute : model.getAttributes()) {
      methodBuilder.addStatement(
          "this.$1N = model.$2N()",
          attribute.getIdentifier(),
          attribute.getGetterToOverride().getSimpleName()
      );
    }

    MethodSpec method = methodBuilder.build();
    this.logger.trace("Generated copy constructor\n{}", method);
    return method;
  }

  private MethodSpec createAllArgsConstructor(Model model) {
    MethodSpec.Builder methodBuilder = MethodSpec
        .constructorBuilder()
        .addModifiers(Modifier.PUBLIC);

    for (Attribute attribute : model.getAttributes()) {
      methodBuilder
          .addParameter(
              attribute.getType(),
              attribute.getIdentifier(),
              Modifier.FINAL
          )
          .addStatement("this.$1N = $1N", attribute.getIdentifier());
    }

    MethodSpec method = methodBuilder.build();
    this.logger.trace("Generated all-args constructor\n{}", method);
    return method;
  }

  private MethodSpec createNoArgsConstructor(Model model) {
    MethodSpec.Builder methodBuilder = MethodSpec
        .constructorBuilder()
        .addModifiers(Modifier.PUBLIC);

    MethodSpec method = methodBuilder.build();
    this.logger.trace("Generated no-args constructor\n{}", method);
    return method;
  }
}
