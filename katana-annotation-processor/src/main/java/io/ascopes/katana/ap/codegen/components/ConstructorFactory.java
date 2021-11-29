package io.ascopes.katana.ap.codegen.components;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import io.ascopes.katana.ap.descriptors.Attribute;
import io.ascopes.katana.ap.descriptors.Constructor;
import io.ascopes.katana.ap.descriptors.Model;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;

/**
 * Factory for creating general purpose public constructors.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class ConstructorFactory {

  private final Logger logger;

  /**
   * Initialize this factory.
   */
  public ConstructorFactory() {
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
  public Stream<MethodSpec> create(Model model) {
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

    for (Attribute attribute : model.getAttributes()) {
      methodBuilder.addStatement(
          "this.$N = $L",
          attribute.getIdentifier(),
          this.defaultValueFor(attribute)
      );
    }

    MethodSpec method = methodBuilder.build();
    this.logger.trace("Generated no-args constructor\n{}", method);
    return method;
  }

  private CodeBlock defaultValueFor(Attribute attribute) {
    switch (attribute.getGetterToOverride().getReturnType().getKind()) {
      case BOOLEAN:
        return CodeBlock.of("$L", false);
      case CHAR:
        return CodeBlock.of("$L", '\0');
      case BYTE:
        return CodeBlock.of("$L", (byte) 0);
      case SHORT:
        return CodeBlock.of("$L", (short) 0);
      case INT:
        return CodeBlock.of("$L", 0);
      case LONG:
        return CodeBlock.of("$L", 0L);
      case FLOAT:
        return CodeBlock.of("$L", 0.0F);
      case DOUBLE:
        return CodeBlock.of("$L", 0.0D);
      default:
        return CodeBlock.of("$L", new Object[]{null});
    }
  }
}
