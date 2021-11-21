package io.ascopes.katana.ap.utils;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Simple logger that writes to stderr.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class Logger {

  private static final Pattern TEMPLATE_PATTERN = Pattern.compile("(?<!\\\\)\\{}");
  private static final String TEMPLATE_PLACEHOLDER = "%s";
  private static final PrintStream STREAM = System.err;

  // TODO: make this default to OFF.
  private static volatile Level enabledLevel = Level.INFO;
  private final String name;

  /**
   * Init a logger named after the class that initialized it.
   */
  public Logger() {
    // Frame 0 = getStackTrace()
    // Frame 1 = Logger::<init>()
    // Frame 2 = callee
    this(Thread.currentThread().getStackTrace()[2].getClassName());
  }

  /**
   * Initialize a logger from a given class.
   *
   * @param thisClass the class to initialize the name from.
   */
  public Logger(Class<?> thisClass) {
    this(thisClass.getCanonicalName());
  }

  /**
   * @param name the name to give this logger.
   */
  private Logger(String name) {
    this.name = name;
  }

  /**
   * Log a message as an info log, if enabled.
   *
   * @param template the template string to use.
   * @param args     the arguments to use.
   */
  public void info(String template, Object @Nullable ... args) {
    this.log(Level.INFO, template, args);
  }

  /**
   * Log a message as a debug log, if enabled.
   *
   * @param template the template string to use.
   * @param args     the arguments to use.
   */
  public void debug(String template, Object @Nullable ... args) {
    this.log(Level.DEBUG, template, args);
  }

  private void log(Level level, String template, Object... args) {
    if (level.ordinal() >= enabledLevel.ordinal()) {
      STREAM.printf("%s - %s - %s - ", LocalDateTime.now(), level, this.name);
      if (args.length > 0) {
        String formatString = TEMPLATE_PATTERN.matcher(template).replaceAll(TEMPLATE_PLACEHOLDER);
        STREAM.printf(formatString, args);
        STREAM.println();
      } else {
        STREAM.println(template);
      }
    }
  }

  /**
   * Set the global logging level.
   *
   * @param level the global logging level to set.
   */
  public static void setGlobalLevel(Level level) {
    enabledLevel = level;
  }

  /**
   * Valid logging levels. These are accessed via their name at runtime.
   */
  @SuppressWarnings("unused")
  public enum Level {
    DEBUG,
    INFO,
    OFF,
  }
}
