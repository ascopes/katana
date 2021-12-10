package io.ascopes.katana.ap.methods;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import io.ascopes.katana.ap.attributes.AttributeDescriptor;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.types.Constructor;
import io.ascopes.katana.ap.types.ModelDescriptor;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;

/**
 * Factory for creating general purpose public constructors.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class ConstructorSourceFactory {

  private final Logger logger;

  /**
   * Initialize this factory.
   */
  public ConstructorSourceFactory() {
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  /**
   * Create constructors for the given model.
   *
   * <p>This will not include constructors generated for builder integration.
   *
   * @param modelDescriptor the model to use.
   * @return a stream of generated method specs.
   */
  public Stream<MethodSpec> create(ModelDescriptor modelDescriptor) {
    return modelDescriptor
        .getConstructors()
        .stream()
        .map(constructor -> this.createConstructor(modelDescriptor, constructor));
  }

  private MethodSpec createConstructor(ModelDescriptor modelDescriptor, Constructor constructor) {
    switch (constructor) {
      case COPY:
        return this.createCopyConstructor(modelDescriptor);
      case NO_ARGS:
        return this.createNoArgsConstructor(modelDescriptor);
      case ALL_ARGS:
        return this.createAllArgsConstructor(modelDescriptor);
      default:
        throw new UnsupportedOperationException("Unknown constructor type " + constructor);
    }
  }

  private MethodSpec createCopyConstructor(ModelDescriptor modelDescriptor) {
    TypeName interfaceType = TypeName.get(modelDescriptor.getSuperInterface().asType());

    MethodSpec.Builder methodBuilder = MethodSpec
        .constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(interfaceType, "model", Modifier.FINAL);

    for (AttributeDescriptor attributeDescriptor : modelDescriptor.getAttributes()) {
      methodBuilder.addStatement(
          "this.$1N = model.$2N()",
          attributeDescriptor.getIdentifier(),
          attributeDescriptor.getGetterToOverride().getSimpleName()
      );
    }

    MethodSpec method = methodBuilder.build();
    this.logger.trace("Generated copy constructor\n{}", method);
    return method;
  }

  private MethodSpec createAllArgsConstructor(ModelDescriptor modelDescriptor) {
    MethodSpec.Builder methodBuilder = MethodSpec
        .constructorBuilder()
        .addModifiers(Modifier.PUBLIC);

    for (AttributeDescriptor attributeDescriptor : modelDescriptor.getAttributes()) {
      methodBuilder
          .addParameter(
              attributeDescriptor.getType(),
              attributeDescriptor.getIdentifier(),
              Modifier.FINAL
          )
          .addStatement("this.$1N = $1N", attributeDescriptor.getIdentifier());
    }

    MethodSpec method = methodBuilder.build();
    this.logger.trace("Generated all-args constructor\n{}", method);
    return method;
  }

  private MethodSpec createNoArgsConstructor(ModelDescriptor modelDescriptor) {
    MethodSpec.Builder methodBuilder = MethodSpec
        .constructorBuilder()
        .addModifiers(Modifier.PUBLIC);

    MethodSpec method = methodBuilder.build();
    this.logger.trace("Generated no-args constructor\n{}", method);
    return method;
  }
}
