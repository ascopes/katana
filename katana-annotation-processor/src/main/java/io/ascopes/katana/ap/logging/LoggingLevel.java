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
