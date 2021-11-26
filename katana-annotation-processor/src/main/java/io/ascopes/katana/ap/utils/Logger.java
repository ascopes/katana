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

  // TODO(ascopes): make this default to OFF.
  private static volatile Level enabledLevel = Level.INFO;

  public void error(String template, Object @Nullable ... args) {
    this.log(Level.ERROR, template, args);
  }

  public void info(String template, Object @Nullable ... args) {
    this.log(Level.INFO, template, args);
  }

  public void debug(String template, Object @Nullable ... args) {
    this.log(Level.DEBUG, template, args);
  }

  public void trace(String template, Object @Nullable ... args) {
    this.log(Level.TRACE, template, args);
  }


  private void log(Level level, String template, Object... args) {
    if (level.ordinal() >= enabledLevel.ordinal()) {
      // Frame 0: getStackTrace
      // Frame 1: log
      // Frame 2: info/debug/trace/etc
      // Frame 3: callee
      StackTraceElement frame = Thread.currentThread().getStackTrace()[3];
      String className = frame.getClassName();
      int lineNumber = frame.getLineNumber();
      STREAM.printf("%s - %s - %s:%d - ", LocalDateTime.now(), level, className, lineNumber);
      if (args.length > 0) {
        String formatString = TEMPLATE_PATTERN.matcher(template).replaceAll(TEMPLATE_PLACEHOLDER);
        STREAM.printf(formatString, args);
        STREAM.println();
      } else {
        STREAM.println(template);
      }
    }
  }

  public static void setGlobalLevel(Level level) {
    enabledLevel = level;
  }

  @SuppressWarnings("unused")
  public enum Level {
    TRACE,
    DEBUG,
    INFO,
    ERROR,
    OFF;

    /**
     * @return the finest logging level.
     */
    public static Level all() {
      assert values().length > 0 : "No logger values!";
      return values()[0];
    }
  }
}
