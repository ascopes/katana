package io.ascopes.katana.ap.utils;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import org.checkerframework.common.util.report.qual.ReportCreation;
import org.checkerframework.common.util.report.qual.ReportInherit;

/**
 * Utilities for naming processing and manipulation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@ReportCreation
@ReportInherit
public final class NamingUtils {

  private NamingUtils() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Make a name suitable for use as an identifier, if it is not appropriate already.
   *
   * <p>This assumes the most recently supported language version. This may vary in behaviour
   * depending on the JDK you use.
   *
   * @param name the name to manipulate.
   * @return the identifier to use.
   */
  public static String transmogrifyIdentifier(String name) {
    Objects.requireNonNull(name);

    if (SourceVersion.isKeyword(name)) {
      return "$__" + name + "__";
    }

    return name;
  }

  /**
   * Convert the given name into pascal-case from camel-case or snake-case.
   * @param name the input name.
   * @return the resultant name.
   */
  public static String toPascalCase(String name) {
    // TODO(ascopes): unit tests

    StringBuilder constructedName = new StringBuilder();
    for (String chunk : name.split("_+")) {
      constructedName.append(capitalize(chunk));
    }

    if (constructedName.length() == 0) {
      throw new IllegalStateException("Empty name produced!");
    }

    return constructedName.toString();
  }

  /**
   * Make the first character of a string uppercase.
   *
   * @param text the text to process.
   * @return the input with the first character changed to uppercase.
   */
  public static String capitalize(String text) {
    // TODO(ascopes): unit tests

    if (text.length() <= 1) {
      return text.toUpperCase(Locale.ROOT);
    }

    return Character.toUpperCase(text.charAt(0)) + text.substring(1);
  }

  /**
   * Remove the given prefix from a method's name, if it is present, and convert the remaining name
   * to camel case.
   *
   * @param method the method to convert.
   * @param prefix the prefix.
   * @return an empty optional if no prefix is found, or the result in an optional.
   */
  public static Optional<String> removePrefixCamelCase(ExecutableElement method, String prefix) {
    // TODO(ascopes): unit tests
    String name = method.getSimpleName().toString();
    int prefixLength = prefix.length();

    if (prefixLength == 0) {
      // The prefix may be empty if we are using fluent naming.
      return Optional.of(method.getSimpleName().toString());
    }

    if (name.length() - prefixLength < 0 || !name.startsWith(prefix)) {
      return Optional.empty();
    }

    String unprefixed = name.substring(prefixLength);
    char firstChar = Character.toLowerCase(unprefixed.charAt(0));
    return Optional.of(firstChar + unprefixed.substring(1));
  }

  /**
   * Concatenate a prefix and a name together to make a method name. If the prefix is empty, then
   * the returned name will match a fluent naming style.
   *
   * <p>For example, {@code addPrefixCamelCase("get", "userName")} will return
   * {@code "getUserName"}, whereas {@code addPrefixCamelCase("", "userName")} would return {@code
   * "userName"} instead.
   *
   * @param prefix the prefix to add to the name.
   * @param name   the name itself.
   * @return the camel-case method name.
   */
  public static String addPrefixCamelCase(String prefix, String name) {
    // TODO(ascopes): unit tests
    prefix = prefix.trim();

    // If there is no content, then it is likely a fluent method name, so don't transform
    // the first character of the name.
    if (prefix.isEmpty()) {
      return name;
    }

    char firstChar = Character.toUpperCase(name.charAt(0));
    return prefix + firstChar + name.substring(1);
  }

  /**
   * Validate that a string is a valid Java class name.
   *
   * @param className the identifier to validate.
   * @throws IllegalArgumentException if invalid.
   */
  public static void validateClassName(String className) throws IllegalArgumentException {
    try {
      validateIdentifier(className);
    } catch (IllegalArgumentException ex) {
      throwInvalidClassName(className, ex.getMessage(), ex);
    }
  }

  /**
   * Validate that a package is a valid Java package name.
   *
   * @param packageName the package name to validate.
   * @throws IllegalArgumentException if invalid.
   */
  public static void validatePackageName(String packageName) throws IllegalArgumentException {
    Objects.requireNonNull(packageName);

    if (packageName.isEmpty()) {
      // Empty packages are allowed.
      return;
    }

    if (packageName.startsWith(".")) {
      throwInvalidPackageName(packageName, "cannot start with a period", null);
    }

    if (packageName.endsWith(".")) {
      throwInvalidPackageName(packageName, "cannot end with a period", null);
    }

    if (packageName.contains("..")) {
      throwInvalidPackageName(packageName, "cannot contain empty level names", null);
    }

    for (String fragment : packageName.split("\\.")) {
      try {
        validateIdentifier(fragment);
      } catch (IllegalArgumentException ex) {
        throwInvalidPackageName(packageName, ex.getMessage(), ex);
      }
    }
  }

  /**
   * Validate that a string is a valid Java identifier.
   *
   * @param identifier the identifier to validate.
   * @throws IllegalArgumentException if invalid.
   */
  static void validateIdentifier(String identifier) throws IllegalArgumentException {
    Objects.requireNonNull(identifier);

    if (identifier.isEmpty()) {
      throwInvalidIdentifier(identifier, "cannot be empty");
    }

    // Keep behaviour consistent with Java 9 by disallowing '_'
    if (identifier.equals("_") || SourceVersion.isKeyword(identifier)) {
      throwInvalidIdentifier(identifier, "is a reserved keyword in Java");
    }

    if (!SourceVersion.isIdentifier(identifier)) {
      throwInvalidIdentifier(identifier, "is not a valid Java identifier");
    }
  }

  private static void throwInvalidIdentifier(String name, String reason) {
    // TODO: use StringUtils#quoted
    throw new IllegalArgumentException("name '" + name + "' " + reason);
  }

  private static void throwInvalidClassName(String name, String reason, Throwable cause) {
    // TODO: use StringUtils#quoted
    throw new IllegalArgumentException(
        "invalid class name '" + name + "': " + reason,
        cause
    );
  }

  private static void throwInvalidPackageName(String name, String reason, Throwable cause) {
    // TODO: use StringUtils#quoted
    throw new IllegalArgumentException(
        "invalid package name '" + name + "': " + reason,
        cause
    );
  }
}
