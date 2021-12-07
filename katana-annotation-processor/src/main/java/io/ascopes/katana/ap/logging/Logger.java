package io.ascopes.katana.ap.logging;

/**
 * Definition of a logger.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public interface Logger {

  boolean isEnabled(LoggingLevel level);

  void error(String format, Object... args);

  void info(String format, Object... args);

  void debug(String format, Object... args);

  void trace(String format, Object... args);
}
