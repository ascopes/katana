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

package io.ascopes.katana.ap.methods;

import io.ascopes.katana.ap.utils.StringUtils;
import java.util.Objects;
import javax.lang.model.element.ExecutableElement;

/**
 * Equality checking strategy.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public abstract class EqualityStrategyDescriptor {

  private EqualityStrategyDescriptor() {
    // sealed class.
  }

  /**
   * A strategy for generating a default implementation where attributes may be explicitly included
   * or excluded.
   */
  public static final class GeneratedEqualityStrategyDescriptor extends EqualityStrategyDescriptor {

    private final boolean includeAll;

    /**
     * Initialize this strategy.
     *
     * @param includeAll true if the strategy includes all attributes by default, or false if it
     *                   excludes all attributes by default.
     */
    public GeneratedEqualityStrategyDescriptor(boolean includeAll) {
      this.includeAll = includeAll;
    }

    /**
     * Return true if the strategy includes all attributes by default, or false if it excludes all
     * attributes by default.
     *
     * @return true or false.
     */
    public boolean isIncludeAll() {
      return this.includeAll;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return "GeneratedEqualityStrategyDescriptor{"
          + "includeAll=" + this.includeAll
          + '}';
    }
  }

  /**
   * A strategy for using a custom implementation of both equals and hashCode, which will be user
   * provided.
   */
  public static final class CustomEqualityStrategyDescriptor extends EqualityStrategyDescriptor {

    private final ExecutableElement equalsMethod;
    private final ExecutableElement hashCodeMethod;

    /**
     * Initialize this strategy.
     *
     * @param equalsMethod   the equals method to call.
     * @param hashCodeMethod the hash code method to call.
     */
    public CustomEqualityStrategyDescriptor(
        ExecutableElement equalsMethod,
        ExecutableElement hashCodeMethod
    ) {
      this.equalsMethod = Objects.requireNonNull(equalsMethod);
      this.hashCodeMethod = Objects.requireNonNull(hashCodeMethod);
    }

    /**
     * Get the custom equals method to call.
     *
     * @return the equals code method.
     */
    public ExecutableElement getEqualsMethod() {
      return this.equalsMethod;
    }

    /**
     * Get the custom hash code method to call.
     *
     * @return the hash code method.
     */
    public ExecutableElement getHashCodeMethod() {
      return this.hashCodeMethod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return "CustomEqualityStrategyDescriptor{"
          + "equalsMethod=" + StringUtils.quoted(this.equalsMethod)
          + "hashCodeMethod=" + StringUtils.quoted(this.hashCodeMethod)
          + '}';
    }
  }
}
