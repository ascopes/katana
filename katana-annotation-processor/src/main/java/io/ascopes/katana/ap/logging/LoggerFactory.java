package io.ascopes.katana.ap.logging;

import io.ascopes.katana.ap.utils.StringUtils;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Singleton provider of loggers. While I could have used something like Logback, that massively
 * increases the initialization time of this annotation processor, and logging is usually just here
 * for my sanity checks, so there is little benefit in doing so.
 *
 * <p>While I had considered JUL logging, that requires other faff to initialize it which may be
 * influenced by stuff in the user's {@code src/main/resources} used for their application.
 * Therefore, to prevent further side effects, I have implemented a basic shim myself instead that
 * loosely resembles the SLF4J API, somewhat.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class LoggerFactory {

  private static final Object lock = new Object();
  private static volatile LoggerFactory instance = null;

  private final RuntimeMXBean runtimeMxBean;
  private final PrintStream outputStream;
  private final Clock clock;
  private volatile LoggingLevel globalLevel;

  private LoggerFactory() {
    this.runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    this.globalLevel = LoggingLevel.INFO;
    this.outputStream = System.out;
    this.clock = Clock.systemDefaultZone();
  }

  private void log(LoggingLevel level, String name, String format, Object... args) {
    if (!this.globalLevel.permits(level)) {
      return;
    }

    String message;
    if (args.length == 0) {
      message = format;
    } else {
      message = String.format(format.replace("{}", "%s"), args);
    }

    this.outputStream.printf(
        "[ %6s ] %s (up %.3fs) - %s - %s%n",
        level.name(),
        LocalDateTime.now(this.clock),
        this.runtimeMxBean.getUptime() / 1_000.0,
        name,
        message
    );
  }

  private final class LoggerImpl implements Logger {

    private final String name;

    private LoggerImpl(String name) {
      String[] parts = name.split("\\.");
      StringBuilder nameBuilder = new StringBuilder();
      for (int i = 0; i < parts.length; ++i) {
        if (i > 0) {
          nameBuilder.append('.');
        }

        if (i + 2 >= parts.length) {
          nameBuilder.append(parts[i]);
        } else {
          nameBuilder.append(parts[i].charAt(0));
        }
      }
      this.name = nameBuilder.toString();
    }

    @Override
    public void error(String format, Object... args) {
      LoggerFactory.this.log(LoggingLevel.ERROR, this.name, format, args);
    }

    @Override
    public void info(String format, Object... args) {
      LoggerFactory.this.log(LoggingLevel.INFO, this.name, format, args);
    }

    @Override
    public void debug(String format, Object... args) {
      LoggerFactory.this.log(LoggingLevel.DEBUG, this.name, format, args);
    }

    @Override
    public void trace(String format, Object... args) {
      LoggerFactory.this.log(LoggingLevel.TRACE, this.name, format, args);
    }
  }

  /**
   * Return a logger for a given class.
   *
   * @param targetClass the class to return the logger for.
   * @return the logger.
   */
  public static Logger loggerFor(Class<?> targetClass) {
    return getInstance().new LoggerImpl(targetClass.getCanonicalName());
  }

  /**
   * Set the global logging level to use.
   *
   * @param level the level to set, as a string.
   * @throws IllegalArgumentException if the logging level cannot be parsed.
   */
  public static void globalLevel(String level) throws IllegalArgumentException {
    try {
      globalLevel(LoggingLevel.parse(level));
    } catch (IllegalArgumentException ex) {
      String validLevelsList = Stream
          .of(LoggingLevel.values())
          .map(StringUtils::quoted)
          .collect(Collectors.joining(", "));

      String message = "Invalid logging level "
          + StringUtils.quoted(level)
          + ", valid levels are: "
          + validLevelsList;

      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Set the global logging level to use.
   *
   * @param level the level to set.
   */
  public static void globalLevel(LoggingLevel level) {
    getInstance().globalLevel = Objects.requireNonNull(level);
  }

  private static LoggerFactory getInstance() {
    if (instance == null) {
      synchronized (lock) {
        if (instance == null) {
          instance = new LoggerFactory();
        }
      }
    }

    return instance;
  }
}
