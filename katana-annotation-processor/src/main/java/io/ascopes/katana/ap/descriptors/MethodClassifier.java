package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.iterators.AvailableMethodsIterator;
import io.ascopes.katana.ap.settings.Setting;
import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import io.ascopes.katana.ap.utils.DiagnosticTemplates;
import io.ascopes.katana.ap.utils.Result;
import java.util.stream.Collectors;
import javax.annotation.processing.Messager;
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
public final class MethodClassifier {

  private final DiagnosticTemplates diagnosticTemplates;
  private final Messager messager;
  private final Types typeUtils;

  /**
   * @param diagnosticTemplates the message templating support to use.
   * @param messager            the messager to report errors with.
   * @param typeUtils           the type utilities to use.
   */
  public MethodClassifier(DiagnosticTemplates diagnosticTemplates, Messager messager,
      Types typeUtils) {
    this.diagnosticTemplates = diagnosticTemplates;
    this.messager = messager;
    this.typeUtils = typeUtils;
  }

  /**
   * @param type     the type to classify the methods on.
   * @param settings the settings to use.
   * @return the methods, or an empty optional if some error occurred and was reported to the
   * compiler.
   */
  public Result<ClassifiedMethods> classifyMethods(
      TypeElement type,
      SettingsCollection settings
  ) {
    ClassifiedMethods.Builder builder = ClassifiedMethods.builder();

    boolean failed = false;

    for (
        AvailableMethodsIterator it = new AvailableMethodsIterator(this.typeUtils, type);
        it.hasNext();
    ) {
      ExecutableElement method = it.next();

      failed |= this.processAsGetter(builder, method, settings)
          // TODO(ascopes): reimplement setter + wither types in the future.
          .ifIgnoredReplace(() -> this.processAsInstanceMethod(builder, method))
          .ifIgnoredReplace(() -> this.processAsStaticMethod(builder, method))
          .assertNotIgnored(() -> "No method processors consumed method " + method)
          .isNotOk();
    }

    return failed
        ? Result.fail()
        : Result.ok(builder.build());
  }

  private Result<Void> processAsGetter(
      ClassifiedMethods.Builder builder,
      ExecutableElement method,
      SettingsCollection settings
  ) {
    String booleanGetterPrefix = settings.getBooleanGetterPrefix().getValue();
    String getterPrefix = settings.getGetterPrefix().getValue();

    if (!method.getParameters().isEmpty()) {
      return Result.ignore();
    }

    if (method.getReturnType().getKind() == TypeKind.VOID) {
      return Result.ignore();
    }

    if (method.getModifiers().contains(Modifier.STATIC)) {
      return Result.ignore();
    }

    return this
        .removePrefixCamelCase(method, booleanGetterPrefix)
        .ifOkFlatMap(attrName -> {
          if (!this.isBooleanReturnType(method, settings)) {
            this.failNonBooleanGetter(method, settings);
            return Result.fail();
          } else {
            return Result.ok(attrName);
          }
        })
        .ifIgnoredReplace(() -> this.removePrefixCamelCase(method, getterPrefix))
        .ifOkFlatMap(attributeName -> {
          ExecutableElement existingMethod = builder.getGetters().get(attributeName);
          if (existingMethod != null) {
            this.failMethodAlreadyExists( existingMethod, method);
            return Result.fail();
          }
          return Result.ok(attributeName);
        })
        .ifOkThen(attributeName -> builder.getter(attributeName, method))
        .thenDiscardValue();
  }

  private Result<Void> processAsInstanceMethod(
      ClassifiedMethods.Builder builder,
      ExecutableElement method
  ) {
    if (method.getModifiers().contains(Modifier.STATIC)) {
      return Result.ignore();
    }

    builder.instanceMethod(method);
    return Result.ok();
  }

  private Result<Void> processAsStaticMethod(
      ClassifiedMethods.Builder builder,
      ExecutableElement method
  ) {
    // I know that this never occurs, but this keeps the interface consistent if I choose to
    // add stuff in the future.
    if (!method.getModifiers().contains(Modifier.STATIC)) {
      return Result.ignore();
    }

    builder.staticMethod(method);
    return Result.ok();
  }

  private Result<String> removePrefixCamelCase(ExecutableElement method, String prefix) {
    String name = method.getSimpleName().toString();
    int prefixLength = prefix.length();

    // The prefix may be empty if we are using fluent naming.
    if (name.length() - prefixLength <= 0 || !name.startsWith(prefix)) {
      return Result.ignore();
    }

    String unprefixed = name.substring(prefixLength);
    char firstChar = Character.toLowerCase(unprefixed.charAt(0));
    return Result.ok(firstChar + unprefixed.substring(1));
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

    if (returnType.getKind() == TypeKind.BOOLEAN) {
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

    String message = this.diagnosticTemplates
        .template("overloadedMethodForAttribute")
        .placeholder("existingTypeName", existingTypeName)
        .placeholder("existingMethodSignature", existingMethodSignature)
        .placeholder("newTypeName", newTypeName)
        .placeholder("newMethodSignature", newMethodSignature)
        .build();

    this.messager.printMessage(Kind.ERROR, message, newMethod);
  }

  private void failNonBooleanGetter(
      ExecutableElement method,
      SettingsCollection settings
  ) {
    Setting<String> booleanGetterPrefix = settings.getBooleanGetterPrefix();
    Setting<String> getterPrefix = settings.getGetterPrefix();
    Setting<Class<?>[]> booleanTypes = settings.getBooleanTypes();

    String message = this.diagnosticTemplates
        .template("nonBooleanGetter")
        .placeholder("method", method.toString())
        .placeholder("booleanGetterPrefix", booleanGetterPrefix.getValue())
        .placeholder("booleanGetterPrefixProperty", booleanGetterPrefix.getDescription())
        .placeholder("getterPrefix", getterPrefix.getValue())
        .placeholder("getterPrefixProperty", getterPrefix.getDescription())
        .placeholder("booleanTypes", booleanTypes.getValue())
        .placeholder("booleanTypesProperty", booleanTypes.getDescription())
        .build();

    this.messager.printMessage(Kind.ERROR, message, method);
  }
}
