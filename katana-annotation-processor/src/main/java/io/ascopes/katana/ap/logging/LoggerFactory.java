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
 * <p>
 * While I had considered JUL logging, that requires other faff to initialize it which may be
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
  private volatile LoggingLevel globalLevel;
  private final PrintStream outputStream;
  private final Clock clock;

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
      StringBuilder messageBuilder = new StringBuilder();
      int size = format.length();
      int argumentIndex = 0;
      char cCurr;
      char cNext;
      for (int formatIndex = 0; formatIndex < size; ++formatIndex) {
        cCurr = format.charAt(formatIndex);
        cNext = formatIndex + 1 == size
            ? '\0'
            : format.charAt(formatIndex + 1);
        if (cCurr != '{' && cNext != '}') {
          messageBuilder.append(cCurr);
          if (cCurr == '\n') {
            // Indent additional lines.
            messageBuilder.append("    ");
          }
          continue;
        }

        ++formatIndex;
        Object arg = args[argumentIndex++];
        messageBuilder.append(arg);
      }
      message = messageBuilder.toString();
    }

    this.outputStream
        .printf(
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

  public static Logger loggerFor(Class<?> targetClass) {
    return getInstance().new LoggerImpl(targetClass.getCanonicalName());
  }

  public static void globalLevel(String level) {
    try {
      globalLevel(LoggingLevel.parse(level));
    } catch (IllegalArgumentException ex) {
      String validLevelsList =
          Stream
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
