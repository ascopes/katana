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

package io.ascopes.katana.annotations;

/**
 * Visibility of a member.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public enum Visibility {
  /**
   * Corresponds to the {@link java.lang.reflect.Modifier#PUBLIC} modifier.
   */
  PUBLIC,

  /**
   * Corresponds to the {@link java.lang.reflect.Modifier#PROTECTED} modifier.
   */
  PROTECTED,

  /**
   * Corresponds to the implicit {@code PACKAGE_PRIVATE} modifier.
   */
  PACKAGE_PRIVATE,

  /**
   * Corresponds to the {@link java.lang.reflect.Modifier#PRIVATE} modifier.
   */
  PRIVATE,
}
