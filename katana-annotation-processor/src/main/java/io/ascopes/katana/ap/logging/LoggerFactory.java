/*
 * Copyright (C) 2021 Ashley Scopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ascopes.katana.ap.logging;

import io.ascopes.katana.ap.utils.StringUtils;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
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
  private volatile LoggingLevel globalLevel;

  private LoggerFactory() {
    this.runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    this.globalLevel = LoggingLevel.INFO;
    this.outputStream = System.out;
  }

  private void log(LoggingLevel level, String name, String format, Object... args) {
    if (!this.globalLevel.permits(level)) {
      return;
    }

    int argIndex = 0;
    boolean newLine = true;
    for (int i = 0; i < format.length(); ++i) {

      if (newLine) {
        newLine = false;
        this.formatLineStart(level, name);
      }

      char msgChar = format.charAt(i);

      if (msgChar == '{' && i < format.length() - 1 && format.charAt(i + 1) == '}') {
        ++i;

        String arg = Objects.toString(args[argIndex++]);

        for (int j = 0; j < arg.length(); ++j) {
          if (newLine) {
            newLine = false;
            this.formatLineStart(level, name);
          }

          char argChar = arg.charAt(j);
          this.outputStream.print(argChar);

          if (argChar == '\n') {
            newLine = true;
          }
        }

        continue;
      }

      if (msgChar == '\n') {
        newLine = true;
      }

      this.outputStream.print(msgChar);
    }

    if (!newLine) {
      this.outputStream.print('\n');
    }

    this.outputStream.flush();
  }

  private void formatLineStart(LoggingLevel level, String name) {
    this.outputStream.print('[');
    this.outputStream.print(level.name());
    this.outputStream.print("] ");
    this.outputStream.print(this.runtimeMxBean.getUptime());
    this.outputStream.print(" <");
    this.outputStream.print(name);
    this.outputStream.print("> :: ");
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

        // All but the last 2 bits of the package name
        // should be abbreviated if they are more than 2
        // characters long
        if (i + 2 >= parts.length || parts[i].length() <= 2) {
          nameBuilder.append(parts[i]);
        } else {
          nameBuilder.append(parts[i].charAt(0));
        }
      }
      this.name = nameBuilder.toString();
    }

    @Override
    public boolean isEnabled(LoggingLevel level) {
      return LoggerFactory.this.globalLevel.permits(level);
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
