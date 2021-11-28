package io.ascopes.katana.ap.utils;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.util.report.qual.ReportCreation;
import org.checkerframework.common.util.report.qual.ReportInherit;

/**
 * Helpers to expose to Handlebars.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@ReportCreation
@ReportInherit
@SuppressWarnings("unused")
public final class HandlebarsHelpers {

  private HandlebarsHelpers() {
    throw new UnsupportedOperationException("static-only class");
  }

  public static String quoted(@Nullable Object value) {
    return StringUtils.quoted(value);
  }

  public static String a(String thing) {
    return StringUtils.prependAOrAn(thing);
  }

  public static String an(String thing) {
    return StringUtils.prependAOrAn(thing);
  }
}
