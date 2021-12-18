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

package io.ascopes.katana.annotations.internal;

/**
 * Interface for a descriptor describing a feature on an attribute.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public interface AttributeFeature extends Feature {

  /**
   * Determine whether the attribute feature would include all attributes implicitly
   * by default.
   *
   * @return true if the value represents "include all".
   */
  default boolean isIncludeAll() {
    return false;
  }

  /**
   * Determine whether the attribute feature would exclude all attributes implicitly
   * by default.
   *
   * @return true if the value represents "exclude all".
   */
  default boolean isExcludeAll() {
    return false;
  }
}
