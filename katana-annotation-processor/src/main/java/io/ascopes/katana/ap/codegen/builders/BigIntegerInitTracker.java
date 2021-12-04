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
    return CodeBlock.of("new $T($S)", BigInteger.class, value.toString());
  }
}
