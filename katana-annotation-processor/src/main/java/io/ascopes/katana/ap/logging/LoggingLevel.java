package io.ascopes.katana.ap.logging;

import java.util.Locale;

/**
 * Logging level.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public enum LoggingLevel {
  ALL(Integer.MIN_VALUE),
  TRACE(1_000),
  DEBUG(2_000),
  INFO(3_000),
  WARNING(4_000),
  ERROR(5_000),
  OFF(Integer.MAX_VALUE);

  private final int order;

  LoggingLevel(int order) {
    this.order = order;
  }

  public boolean permits(LoggingLevel queryLevel) {
    return this.order <= queryLevel.order;
  }

  public static LoggingLevel parse(String name) {
    return LoggingLevel.valueOf(name.toUpperCase(Locale.ROOT));
  }
}
