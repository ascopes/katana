package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.iterators.AvailableMethodsIterator;
import io.ascopes.katana.ap.logging.Diagnostics;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.settings.Setting;
import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import io.ascopes.katana.ap.utils.NamingUtils;
import io.ascopes.katana.ap.utils.Result;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

/**
 * Classifier for methods. This enables the discovery of getter/setter-like methods.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class MethodClassificationFactory {

  private final Diagnostics diagnostics;
  private final Types typeUtils;
  private final Logger logger;

  /**
   * Initialize this factory.
   *
   * @param diagnostics diagnostics to use for reporting compilation errors.
   * @param typeUtils   the type utilities to use for introspection.
   */
  public MethodClassificationFactory(
      Diagnostics diagnostics,
      Types typeUtils
  ) {
    this.diagnostics = diagnostics;
    this.typeUtils = typeUtils;
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  /**
   * Create a method classification collection for the given interface model type and the given
   * settings.
   *
   * @param interfaceType the interface type to scan.
   * @param settings      the settings to consider.
   * @return the method classification in an OK result, or a failed result if an error occurred.
   */
  public Result<MethodClassification> create(
      TypeElement interfaceType,
      SettingsCollection settings
  ) {
    MethodClassification.Builder builder = MethodClassification.builder();

    boolean failed = false;

    AvailableMethodsIterator it = new AvailableMethodsIterator(this.typeUtils, interfaceType);

    while (it.hasNext()) {
      ExecutableElement method = it.next();

      if (this.isValidGetterSignature(method)) {
        failed |= this.getBooleanAttrName(method, settings)
            .orElseGet(() -> this.getAttrName(method, settings))
            .ifOkCheck(attrName -> this.applyGetter(builder, attrName, method))
            .isNotOk();
      } else {
        failed |= this.processAsStaticMethod(builder, method)
            .orElseGet(() -> {
              this.failUnimplementableMethods(interfaceType, method);
              return Result.fail();
            })
            .isNotOk();
      }
    }

    return failed
        ? Result.fail()
        : Result.ok(builder.build());
  }

  private boolean isValidGetterSignature(ExecutableElement method) {
    if (!this.isInstanceScoped(method)) {
      this.logger.trace("Ignoring {} as getter, method not instance-scoped", method);
      return false;
    }

    if (!method.getParameters().isEmpty()) {
      this.logger.trace("Ignoring {} as getter, method had parameters present", method);
      return false;
    }

    if (method.getReturnType().getKind() == TypeKind.VOID) {
      this.logger.trace("Ignoring {} as getter, method had no return type", method);
      return false;
    }

    return true;
  }

  private Optional<Result<String>> getBooleanAttrName(
      ExecutableElement method,
      SettingsCollection settings
  ) {
    String booleanGetterPrefix = settings.getBooleanGetterPrefix().getValue();

    Optional<Result<String>> result = NamingUtils
        .removePrefixCamelCase(method, booleanGetterPrefix)
        .map(attrName -> {
          if (this.isBooleanReturnType(method, settings)) {
            this.logger.trace("{} is boolean getter for attribute {}", method, attrName);
            return Result.ok(attrName);
          }

          this.failNonBooleanGetter(method, settings);
          return Result.fail();
        });

    if (!result.isPresent()) {
      this.logger.trace(
          "Ignoring {} as boolean getter, it does not match the naming strategy",
          method
      );
    }

    return result;
  }

  private Result<String> getAttrName(ExecutableElement method, SettingsCollection settings) {
    String getterPrefix = settings.getGetterPrefix().getValue();

    String attrName = NamingUtils
        .removePrefixCamelCase(method, getterPrefix)
        .orElseThrow(() -> new RuntimeException("Unreachable!"));
    this.logger.trace("{} is a getter for attribute {}", method, attrName);
    return Result.ok(attrName);
  }

  private Result<Void> applyGetter(
      MethodClassification.Builder builder,
      String attrName,
      ExecutableElement newMethod
  ) {
    return builder
        .getExistingGetter(attrName)
        .map(existingMethod -> {
          this.failMethodAlreadyExists(existingMethod, newMethod);
          return Result.<Void>fail();
        })
        .orElseGet(Result::ok)
        .ifOk(() -> builder.getter(attrName, newMethod));
  }

  private Optional<Result<ExecutableElement>> processAsStaticMethod(
      MethodClassification.Builder builder,
      ExecutableElement method
  ) {
    // I know that this never occurs, but this keeps the interface consistent if I choose to
    // add stuff in the future.
    if (this.isInstanceScoped(method)) {
      this.logger.trace("{} is not a static method", method);
      return Optional.empty();
    }

    this.logger.trace("{} is a static method", method);
    builder.staticMethod(method);
    return Optional.of(Result.ok(method));
  }

  private String signatureOf(ExecutableElement element) {
    String typeParams = element
        .getTypeParameters()
        .stream()
        .map(TypeParameterElement::toString)
        .collect(Collectors.joining(", "));

    if (!typeParams.isEmpty()) {
      typeParams = "<" + typeParams + "> ";
    }

    return typeParams + element.getReturnType() + " " + element;
  }

  private boolean isBooleanReturnType(ExecutableElement method, SettingsCollection settings) {
    TypeMirror returnType = method.getReturnType();

    if (this.isBooleanPrimitiveReturnType(method)) {
      return true;
    }

    if ((returnType instanceof DeclaredType)) {
      DeclaredType declaredReturnType = (DeclaredType) returnType;
      TypeElement declaredTypeElement = (TypeElement) declaredReturnType.asElement();
      String className = declaredTypeElement.getQualifiedName().toString();

      for (Class<?> type : settings.getBooleanTypes().getValue()) {
        if (className.equals(type.getCanonicalName())) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean isBooleanPrimitiveReturnType(ExecutableElement method) {
    return method.getReturnType().getKind() == TypeKind.BOOLEAN;
  }

  private boolean isInstanceScoped(ExecutableElement method) {
    return !method.getModifiers().contains(Modifier.STATIC);
  }

  private void failMethodAlreadyExists(
      ExecutableElement existingMethod,
      ExecutableElement newMethod
  ) {
    TypeElement existingType = (TypeElement) existingMethod.getEnclosingElement();
    String existingTypeName = existingType.getQualifiedName().toString();
    String existingMethodSignature = this.signatureOf(existingMethod);

    TypeElement newType = (TypeElement) newMethod.getEnclosingElement();
    String newTypeName = newType.getQualifiedName().toString();
    String newMethodSignature = this.signatureOf(newMethod);

    this.diagnostics
        .builder()
        .kind(Kind.ERROR)
        .element(newMethod)
        .template("overloadedMethodForAttribute")
        .param("existingTypeName", existingTypeName)
        .param("existingMethodSignature", existingMethodSignature)
        .param("newTypeName", newTypeName)
        .param("newMethodSignature", newMethodSignature)
        .log();
  }

  private void failNonBooleanGetter(
      ExecutableElement method,
      SettingsCollection settings
  ) {
    Setting<String> booleanGetterPrefix = settings.getBooleanGetterPrefix();
    Setting<String> getterPrefix = settings.getGetterPrefix();
    Setting<Class<?>[]> booleanTypes = settings.getBooleanTypes();

    this.diagnostics
        .builder()
        .kind(Kind.ERROR)
        .element(method)
        .template("nonBooleanGetter")
        .param("method", method.toString())
        .param("booleanGetterPrefix", booleanGetterPrefix.getValue())
        .param("booleanGetterPrefixProperty", booleanGetterPrefix.getDescription())
        .param("getterPrefix", getterPrefix.getValue())
        .param("getterPrefixProperty", getterPrefix.getDescription())
        .param("booleanTypes", booleanTypes.getValue())
        .param("booleanTypesProperty", booleanTypes.getDescription())
        .log();
  }

  private void failUnimplementableMethods(TypeElement interfaceType, ExecutableElement method) {
    this.diagnostics
        .builder()
        .kind(Kind.ERROR)
        .element(method)
        .template("unimplementableMethods")
        .param("type", interfaceType.getQualifiedName())
        .param("method", method)
        .log();
  }
}
