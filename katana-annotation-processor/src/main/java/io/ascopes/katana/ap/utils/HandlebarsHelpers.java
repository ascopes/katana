package io.ascopes.katana.ap.utils;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Helpers to expose to Handlebars.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class HandlebarsHelpers {

  private HandlebarsHelpers() {
    throw new UnsupportedOperationException("static-only class");
  }

  public static String quoted(@Nullable Object value) {
    return StringUtils.quoted(value);
  }

  public static String a(String thing) {
    return StringUtils.a(thing);
  }

  public static String an(String thing) {
    return StringUtils.a(thing);
  }
}
