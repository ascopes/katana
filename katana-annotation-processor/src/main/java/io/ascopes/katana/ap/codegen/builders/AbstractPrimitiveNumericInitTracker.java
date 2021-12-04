package io.ascopes.katana.ap.codegen.builders;

import com.squareup.javapoet.CodeBlock;
import io.ascopes.katana.ap.descriptors.Attribute;
import java.util.SortedSet;

/**
 * Primitive numeric initializer tracker.
 *
 * @param <N> the numeric type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
abstract class AbstractPrimitiveNumericInitTracker<N extends Number> 
    extends AbstractNumericInitTracker<N> {

  AbstractPrimitiveNumericInitTracker(
      SortedSet<Attribute> attributes,
      Class<N> type
  ) {
    super(attributes, CodeBlock.of("0"), type);
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
