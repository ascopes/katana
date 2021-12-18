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

package io.ascopes.katana.ap.builders;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import io.ascopes.katana.ap.attributes.AttributeDescriptor;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;

/**
 * Factory for initialization trackers.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
final class InitTrackerFactory {

  private final Logger logger;

  /**
   * Initialize this factory.
   */
  InitTrackerFactory() {
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  /**
   * Create a new tracker for the given stream of required attributes.
   *
   * @param attributeDescriptors the attributes to track.
   * @return a tracker of the most efficient size to use for the given attributes.
   */
  InitTracker create(SortedSet<AttributeDescriptor> attributeDescriptors) {
    // TODO(ascopes): test these size offsets are correct and don't get messed up by int overflows.
    if (attributeDescriptors.size() < Integer.SIZE) {
      this.logger.debug("Using an int tracker for tracking initialized attributes");
      return new IntInitTracker(attributeDescriptors);
    }

    if (attributeDescriptors.size() < Long.SIZE) {
      this.logger.debug("Using a long tracker for tracking initialized attributes");
      return new LongInitTracker(attributeDescriptors);
    }

    this.logger.debug("Using a BigInteger tracker for tracking initialized attributes");
    return new BigIntegerInitTracker(attributeDescriptors);
  }

  /**
   * Init tracker that uses numbers with bitfields.
   *
   * @param <N> the number type.
   */
  private abstract static class NumericInitTracker<N extends Number> implements InitTracker {

    private final Map<AttributeDescriptor, CodeBlock> flags;
    private final CodeBlock zero;
    private final TypeName type;

    NumericInitTracker(
        SortedSet<AttributeDescriptor> attributeDescriptors,
        CodeBlock zero,
        Class<N> type
    ) {
      this.flags = new HashMap<>();
      this.zero = zero;

      int index = 0;
      for (AttributeDescriptor attributeDescriptor : attributeDescriptors) {
        this.flags.put(attributeDescriptor, this.getFlag(index++));
      }

      TypeName typeName = TypeName.get(type);
      this.type = typeName.isBoxedPrimitive()
          ? typeName.unbox()
          : typeName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final TypeName getTrackerType() {
      return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isTrackingVariableFinal() {
      return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final CodeBlock initializeTracker(CodeBlock trackingVariable) {
      return CodeBlock.of("$L = $L", trackingVariable, this.zero);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Optional<CodeBlock> markAttributeInitialized(
        CodeBlock trackingVariable,
        AttributeDescriptor attribute
    ) {
      return Optional
          .ofNullable(this.flags.get(attribute))
          .map(flag -> this.setFlagExpr(trackingVariable, flag));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Optional<CodeBlock> isAttributeUninitialized(
        CodeBlock trackingVariable,
        AttributeDescriptor attribute
    ) {
      return Optional
          .ofNullable(this.flags.get(attribute))
          .map(flag -> this.isNotSetExpr(trackingVariable, flag));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final CodeBlock isAnyUninitialized(CodeBlock trackingVariable) {
      return this.isUnsetExpr(trackingVariable, this.flags.size());
    }

    abstract CodeBlock getFlag(int index);

    abstract CodeBlock isUnsetExpr(CodeBlock variable, int totalItems);

    abstract CodeBlock isNotSetExpr(CodeBlock variable, CodeBlock flag);

    abstract CodeBlock setFlagExpr(CodeBlock variable, CodeBlock flag);
  }

  /**
   * Primitive numeric initializer tracker.
   *
   * @param <N> the numeric type.
   */
  abstract static class ValueNumericInitTracker<N extends Number> extends NumericInitTracker<N> {

    private ValueNumericInitTracker(
        SortedSet<AttributeDescriptor> attributeDescriptors,
        Class<N> type
    ) {
      super(attributeDescriptors, CodeBlock.of("0"), type);
    }

    @Override
    CodeBlock getFlag(int index) {
      return this.valueOf(1L << index);
    }

    @Override
    CodeBlock isUnsetExpr(CodeBlock variable, int totalItems) {
      return CodeBlock.of("(($1L & $2L) != $2L)", variable, this.valueOf((1L << totalItems) - 1));
    }

    @Override
    CodeBlock isNotSetExpr(CodeBlock variable, CodeBlock flag) {
      return CodeBlock.of("(($1L & $2L) != $2L)", variable, flag);
    }

    @Override
    CodeBlock setFlagExpr(CodeBlock variable, CodeBlock flag) {
      return CodeBlock.of("$L |= $L", variable, flag);
    }

    abstract CodeBlock valueOf(long value);
  }

  /**
   * Initialization tracker that uses integer bitfields.
   */
  private static final class IntInitTracker extends ValueNumericInitTracker<Integer> {

    private IntInitTracker(SortedSet<AttributeDescriptor> attributeDescriptors) {
      super(attributeDescriptors, int.class);
    }

    @Override
    CodeBlock valueOf(long value) {
      return CodeBlock.of("0b$L", Integer.toString((int) value, 2));
    }

  }

  /**
   * Initialization tracker that uses long bitfields.
   */
  private static final class LongInitTracker extends ValueNumericInitTracker<Long> {

    private LongInitTracker(SortedSet<AttributeDescriptor> attributeDescriptors) {
      super(attributeDescriptors, long.class);
    }

    @Override
    CodeBlock valueOf(long value) {
      return CodeBlock.of("0b$L", Long.toString(value, 2));
    }

  }

  /**
   * Initialization tracker that uses long bitfields.
   */
  private static final class BigIntegerInitTracker extends NumericInitTracker<BigInteger> {

    private static final boolean jdk9;

    static {
      boolean hasTwo = true;

      try {
        BigInteger.class.getDeclaredField("TWO");
      } catch (NoSuchFieldException ex) {
        hasTwo = false;
      }

      jdk9 = hasTwo;
    }

    private BigIntegerInitTracker(SortedSet<AttributeDescriptor> attributeDescriptors) {
      super(
          attributeDescriptors,
          CodeBlock.of("$T.ZERO", BigInteger.class),
          BigInteger.class
      );
    }

    @Override
    CodeBlock getFlag(int index) {
      return value(BigInteger.ONE.shiftLeft(index));
    }

    @Override
    CodeBlock isUnsetExpr(CodeBlock variable, int totalItems) {
      CodeBlock maxItems = value(BigInteger.ONE.shiftLeft(totalItems).subtract(BigInteger.ONE));
      return CodeBlock.of("($1L.and($2L).equals($2L))", variable, maxItems);
    }

    @Override
    CodeBlock isNotSetExpr(CodeBlock variable, CodeBlock flag) {
      return CodeBlock.of("(!$1L.and($2L).equals($2L))", variable, flag);
    }

    @Override
    CodeBlock setFlagExpr(CodeBlock variable, CodeBlock flag) {
      return CodeBlock.of("$1L = $1L.or($2L)", variable, flag);
    }

    static CodeBlock value(BigInteger value) {
      if (value.equals(BigInteger.ZERO)) {
        return CodeBlock.of("$T.ZERO", BigInteger.class);
      }

      if (value.equals(BigInteger.ONE)) {
        return CodeBlock.of("$T.ONE", BigInteger.class);
      }

      if (value.equals(BigInteger.valueOf(2)) && jdk9) {
        // Only added in JDK9, micro-optimisation for poorly optimising JDKs, useless operation
        // for anything else.
        return CodeBlock.of("$T.TWO", BigInteger.class);
      }

      if (value.equals(BigInteger.TEN)) {
        return CodeBlock.of("$T.TEN", BigInteger.class);
      }

      try {
        long primitiveValue = value.longValueExact();
        String primitiveValueBinary = Long.toString(primitiveValue, 2);
        return CodeBlock.of("$T.valueOf(0b$L)", BigInteger.class, primitiveValueBinary);
      } catch (ArithmeticException ex) {
        // Bigger than a long, fall back to string representation.
        // TODO(ascopes): can I optimise this some other way?
        // Ideally these initializations should be near zero overhead.
        return CodeBlock.of("new $T($S)", BigInteger.class, value.toString());
      }
    }
  }
}
