package io.ascopes.katana.ap.utils;

import javax.lang.model.SourceVersion;

/**
 * Utilities for naming processing and manipulation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public abstract class NamingUtils {

  private NamingUtils() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Validate that a package is a valid Java package name.
   *
   * @param packageName the package name to validate.
   * @throws IllegalArgumentException if invalid.
   */
  public static void validatePackageName(String packageName) throws IllegalArgumentException {
    if (packageName.isEmpty()) {
      // Empty packages are allowed.
      return;
    }

    int start = 0;
    for (int i = start; i < packageName.length(); ++i) {
      char c = packageName.charAt(i);
      if (c == '.') {
        if (start == i || start + 1 >= packageName.length()) {
          throw new IllegalArgumentException(
              "invalid package name '" + packageName + "' at position " + (i + 1)
                  + ": expected an identifier but received '.'"
          );
        }
        start = i;
        continue;
      }

      if (start == i - 1) {
        if (!Character.isJavaIdentifierStart(c)) {
          throw new IllegalArgumentException(
              "invalid package name '" + packageName + "' at position " + (i + 1)
                  + ": first character '" + c + "' is not allowed here"
          );
        }

        continue;
      }

      if (!Character.isJavaIdentifierPart(c)) {
        throw new IllegalArgumentException(
            "invalid package name '" + packageName + "' at position " + (i + 1)
                + ": character '" + c + "' is not allowed here"
        );
      }
    }
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
      throw new IllegalArgumentException(
          "invalid class name '" + className + "': " + ex.getMessage()
      );
    }
  }

  /**
   * Validate that a string is a valid Java identifier.
   *
   * @param identifier the identifier to validate.
   * @throws IllegalArgumentException if invalid.
   */
  public static void validateIdentifier(String identifier) throws IllegalArgumentException {
    if (identifier.isEmpty()) {
      throw new IllegalArgumentException("empty identifier");
    }

    char c = identifier.charAt(0);

    if (!Character.isJavaIdentifierStart(c)) {
      throw new IllegalArgumentException("invalid character '" + c + "' at position 1");
    }

    for (int i = 1; i < identifier.length(); ++i) {
      c = identifier.charAt(i);
      if (!Character.isJavaIdentifierPart(c)) {
        throw new IllegalArgumentException("invalid character '" + c + "' at position " + (i + 1));
      }
    }
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
    if (SourceVersion.isKeyword(name)) {
      return "$__" + name + "__";
    }

    return name;
  }
}
