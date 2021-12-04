package io.ascopes.katana.ap.codegen.builders;

import com.squareup.javapoet.CodeBlock;
import io.ascopes.katana.ap.descriptors.Attribute;
import java.math.BigInteger;
import java.util.SortedSet;

/**
 * Initialization tracker that uses long bitfields.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
class BigIntegerInitTracker extends AbstractNumericInitTracker<BigInteger> {
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

  BigIntegerInitTracker(SortedSet<Attribute> attributes) {
    super(
        attributes,
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
