package io.ascopes.katana.ap.utils;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.util.report.qual.ReportCreation;
import org.checkerframework.common.util.report.qual.ReportInherit;

/**
 * Helper methods for strings.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@ReportCreation
@ReportInherit
public final class StringUtils {

  private StringUtils() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Wrap a string representation of some object in quotes.
   *
   * <p>Any {@code "} characters or {@code \} characters within the string will be escaped.
   *
   * @param value the object to convert to a string and quote.
   * @return the quoted string.
   */
  public static String quoted(@Nullable Object value) {
    String raw = Objects.toString(value);
    StringBuilder builder = new StringBuilder("\"");

    for (int i = 0; i < raw.length(); ++i) {
      char c = raw.charAt(i);

      switch (c) {
        case '\\':
          builder.append("\\\\");
          break;
        case '"':
          builder.append("\\\"");
          break;
        default:
          builder.append(c);
          break;
      }
    }

    return builder.append("\"").toString();
  }

  /**
   * Convert the given element to a string and prefix it with "{@code a}" if it begins with a
   * consonant, or "{@code an}" if it begins with a vowel.
   *
   * @param element the element to convert to a string and prepend a conjunction to.
   * @return the resultant string.
   */
  public static String prependAOrAn(@Nullable Object element) {
    if (element == null) {
      return "null";
    }

    String name = Objects.toString(element);

    if (name.isEmpty()) {
      return "empty";
    }

    int index = 0;

    // Skip stuff like punctuation.
    while (index < name.length() && !Character.isLetterOrDigit(name.charAt(0))) {
      ++index;
    }

    if (index >= name.length()) {
      // TODO: unit test this case
      return ("a " + name).trim();
    }

    switch (Character.toLowerCase(name.charAt(index))) {
      // Eight starts with an 'e' sound.
      case '8':  // TODO: unit test this case
      case 'a':
      case 'e':
      case 'i':
      case 'o':
      case 'u':
        return "an " + name;
      default:
        return "a " + name;
    }
  }
}
