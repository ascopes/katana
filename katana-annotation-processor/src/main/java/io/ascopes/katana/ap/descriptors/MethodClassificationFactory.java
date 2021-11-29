package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.annotations.Equality;
import io.ascopes.katana.annotations.ToString;
import io.ascopes.katana.ap.iterators.AvailableMethodsIterator;
import io.ascopes.katana.ap.logging.Diagnostics;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.settings.Setting;
import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import io.ascopes.katana.ap.utils.NamingUtils;
import io.ascopes.katana.ap.utils.Result;
import java.util.stream.Collectors;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
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
  private final Elements elementUtils;
  private final Types typeUtils;
  private final Logger logger;

  /**
   * Initialize this factory.
   *
   * @param diagnostics  diagnostics to use for reporting compilation errors.
   * @param elementUtils the element utilities to use for introspection.
   * @param typeUtils    the type utilities to use for introspection.
   */
  public MethodClassificationFactory(
      Diagnostics diagnostics,
      Elements elementUtils,
      Types typeUtils
  ) {
    this.diagnostics = diagnostics;
    this.elementUtils = elementUtils;
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

      failed |= this
          .processAsGetter(builder, method, settings)
          .ifIgnoredReplace(() -> this.processEquals(builder, method, settings, interfaceType))
          .ifIgnoredReplace(() -> this.processHashCode(builder, method, settings, interfaceType))
          .ifIgnoredReplace(() -> this.processToString(builder, method, settings, interfaceType))
          // TODO(ascopes): reimplement setter + wither types in the future.
          .ifIgnoredReplace(() -> this.processAsStaticMethod(builder, method))
          .ifIgnoredReplace(() -> {
            this.failUnimplementableMethods(interfaceType, method);
            return Result.fail();
          })
          .isNotOk();
    }

    return failed
        ? Result.fail()
        : Result.ok(builder.build());
  }

  private Result<Void> processAsGetter(
      MethodClassification.Builder builder,
      ExecutableElement method,
      SettingsCollection settings
  ) {
    String booleanGetterPrefix = settings.getBooleanGetterPrefix().getValue();
    String getterPrefix = settings.getGetterPrefix().getValue();

    boolean interesting = method.getParameters().isEmpty()
        && method.getReturnType().getKind() != TypeKind.VOID
        && this.isInstanceScoped(method);

    if (!interesting) {
      this.logger.trace(
          "Ignoring {} as a getter as it does not match the desired signature",
          method
      );
      return Result.ignore();
    }

    return NamingUtils
        .removePrefixCamelCase(method, booleanGetterPrefix)
        .ifOkFlatMap(attrName -> {
          if (!this.isBooleanReturnType(method, settings)) {
            this.failNonBooleanGetter(method, settings);
            return Result.fail();
          } else {
            this.logger.trace("{} is a valid boolean getter", method);
            return Result.ok(attrName);
          }
        })
        .ifIgnoredReplace(() -> NamingUtils.removePrefixCamelCase(method, getterPrefix))
        .ifOkFlatMap(attributeName -> builder
            .getExistingGetter(attributeName)
            .map(existingGetter -> {
              this.failMethodAlreadyExists(existingGetter, method);
              return Result.<String>fail();
            })
            .orElseGet(() -> {
              this.logger.trace("{} is a valid getter", method);
              return Result.ok(attributeName);
            }))
        .ifOkThen(attributeName -> builder.getter(attributeName, method))
        .thenDiscardValue()
        .ifIgnoredThen(() -> this.logger.trace(
            "Ignoring {} as a getter as it does not match any of the the required names",
            method
        ));
  }

  private Result<Void> processEquals(
      MethodClassification.Builder builder,
      ExecutableElement method,
      SettingsCollection settings,
      TypeElement interfaceTypeElement
  ) {
    if (settings.getEqualityMode().getValue() != Equality.CUSTOM) {
      this.logger.trace(
          "Ignoring {} as custom equality method as the feature is not set to CUSTOM",
          method
      );
      return Result.ignore();
    }

    TypeMirror thisType = interfaceTypeElement.asType();
    TypeMirror objectType = this.elementUtils
        .getTypeElement(Object.class.getCanonicalName())
        .asType();

    // static boolean isEqual(ModelType first, Object second);
    String hashCodeMethodName = settings.getEqualsMethodName().getValue();
    boolean interesting = method.getSimpleName().contentEquals(hashCodeMethodName)
        && !this.isInstanceScoped(method)
        && this.isBooleanPrimitiveReturnType(method)
        && method.getParameters().size() == 2
        && this.typeUtils.isAssignable(thisType, method.getParameters().get(0).asType())
        && this.typeUtils.isSameType(objectType, method.getParameters().get(1).asType());

    if (!interesting) {
      this.logger.trace(
          "Ignoring {} as custom equality method as it does not match the desired format",
          method
      );

      return Result.ignore();
    }

    builder.equalsImplementation(method);
    return Result.ok();
  }

  private Result<Void> processHashCode(
      MethodClassification.Builder builder,
      ExecutableElement method,
      SettingsCollection settings,
      TypeElement interfaceTypeElement
  ) {
    if (settings.getEqualityMode().getValue() != Equality.CUSTOM) {
      this.logger.trace(
          "Ignoring {} as custom hashCode method as the feature is not set to CUSTOM",
          method
      );
      return Result.ignore();
    }

    TypeMirror thisType = interfaceTypeElement.asType();

    // static int hashCodeOf(ModelType first);
    String hashCodeMethodName = settings.getHashCodeMethodName().getValue();
    boolean interesting = method.getSimpleName().contentEquals(hashCodeMethodName)
        && !this.isInstanceScoped(method)
        && method.getReturnType().getKind() == TypeKind.INT
        && method.getParameters().size() == 1
        && this.typeUtils.isAssignable(thisType, method.getParameters().get(0).asType());

    if (!interesting) {
      this.logger.trace(
          "Ignoring {} as custom hashCode method as it does not match the desired format",
          method
      );
      return Result.ignore();
    }

    builder.hashCodeImplementation(method);
    return Result.ok();
  }

  private Result<Void> processToString(
      MethodClassification.Builder builder,
      ExecutableElement method,
      SettingsCollection settings,
      TypeElement interfaceTypeElement
  ) {
    if (settings.getToStringMode().getValue() != ToString.CUSTOM) {
      this.logger.trace(
          "Ignoring {} as custom toString method as the feature is not set to CUSTOM",
          method
      );
      return Result.ignore();
    }

    TypeMirror thisType = interfaceTypeElement.asType();
    TypeMirror stringType = this.elementUtils
        .getTypeElement(String.class.getCanonicalName())
        .asType();

    // static String asString(ModelType first);
    String toStringMethodName = settings.getToStringMethodName().getValue();
    boolean interesting = method.getSimpleName().contentEquals(toStringMethodName)
        && !this.isInstanceScoped(method)
        && this.typeUtils.isSameType(method.getReturnType(), stringType)
        && method.getParameters().size() == 1
        && this.typeUtils.isAssignable(thisType, method.getParameters().get(0).asType());

    if (!interesting) {
      this.logger.trace(
          "Ignoring {} as custom toString method as it does not match the desired format",
          method
      );
      return Result.ignore();
    }

    builder.toStringImplementation(method);
    return Result.ok();
  }

  private Result<Void> processAsStaticMethod(
      MethodClassification.Builder builder,
      ExecutableElement method
  ) {
    // I know that this never occurs, but this keeps the interface consistent if I choose to
    // add stuff in the future.
    if (this.isInstanceScoped(method)) {
      this.logger.trace("{} is not a static method");
      return Result.ignore();
    }

    this.logger.trace("{} is a static method");
    builder.staticMethod(method);
    return Result.ok();
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

  private void failUnimplementableMethods(TypeElement selfType, ExecutableElement method) {
    this.diagnostics
        .builder()
        .kind(Kind.ERROR)
        .element(method)
        .template("unimplementableMethods")
        .param("type", selfType.getQualifiedName())
        .param("method", method)
        .log();
  }
}
