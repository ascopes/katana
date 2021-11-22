package io.ascopes.katana.ap.codegen.init;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import io.ascopes.katana.ap.descriptors.Attribute;
import java.util.SortedSet;

/**
 * Tracker for less than 64 attributes, which uses a primitive long to store the initialization
 * state.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
class LongInitTracker extends AbstractInitTracker {
  LongInitTracker(SortedSet<Attribute> attributeSet) {
    super(attributeSet);
  }

  @Override
  CodeBlock cast(int value) {
    return CodeBlock.of("$LL", value);
  }

  @Override
  CodeBlock and(CodeBlock left, CodeBlock right) {
    return CodeBlock.of("($L & $L)", left, right);
  }

  @Override
  CodeBlock or(CodeBlock left, CodeBlock right) {
    return CodeBlock.of("($L | $L)", left, right);
  }

  @Override
  CodeBlock shl(CodeBlock left, CodeBlock right) {
    return CodeBlock.of("($L << $L)", left, right);
  }

  @Override
  CodeBlock eq(CodeBlock left, CodeBlock right) {
    return CodeBlock.of("($L == $L)", left, right);
  }

  @Override
  public TypeName getTypeName() {
    return TypeName.LONG;
  }
}
