package io.ascopes.katana.ap.codegen.components;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import io.ascopes.katana.ap.descriptors.Attribute;
import io.ascopes.katana.ap.descriptors.Constructor;
import io.ascopes.katana.ap.descriptors.Model;
import io.ascopes.katana.ap.utils.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;

/**
 * Factory for creating general purpose public constructors.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class ConstructorFactory {

  private final Logger logger;

  public ConstructorFactory() {
    this.logger = new Logger();
  }

  public Iterable<MethodSpec> create(Model model) {
    Set<Constructor> constructorsToBuild = model.getConstructors();
    List<MethodSpec> generatedConstructors = new ArrayList<>();

    for (Constructor constructor : constructorsToBuild) {
      switch (constructor) {
        case COPY:
          generatedConstructors.add(this.createCopyConstructor(model));
          break;
        case NO_ARGS:
          generatedConstructors.add(this.createNoArgsConstructor(model));
          break;
        case ALL_ARGS:
          generatedConstructors.add(this.createAllArgsConstructor(model));
          break;
      }
    }

    return generatedConstructors;
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
