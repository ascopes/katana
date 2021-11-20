package io.ascopes.katana.ap.utils;

import java.util.Objects;
import javax.lang.model.SourceVersion;
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
public abstract class NamingUtils {

  private NamingUtils() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Make a name suitable for use as an identifier, if it is not appropriate already.
   * <p>
   * This assumes the most recently supported language version. This may vary in behaviour
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
    throw new IllegalArgumentException("name '" + name + "' " + reason);
  }

  private static void throwInvalidClassName(String name, String reason, Throwable cause) {
    throw new IllegalArgumentException(
        "invalid class name '" + name + "': " + reason,
        cause
    );
  }

  private static void throwInvalidPackageName(String name, String reason, Throwable cause) {
    throw new IllegalArgumentException(
        "invalid package name '" + name + "': " + reason,
        cause
    );
  }
}
