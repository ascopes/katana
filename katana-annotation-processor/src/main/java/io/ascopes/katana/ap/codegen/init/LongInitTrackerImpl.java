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
final class LongInitTrackerImpl extends AbstractInitTracker {

  /**
   * Initialize this tracker.
   *
   * @param attributeSet the attribute set of required attributes.
   */
  LongInitTrackerImpl(SortedSet<Attribute> attributeSet) {
    super(attributeSet);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  CodeBlock cast(int value) {
    return CodeBlock.of("$LL", value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  CodeBlock and(CodeBlock left, CodeBlock right) {
    return CodeBlock.of("($L & $L)", left, right);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  CodeBlock or(CodeBlock left, CodeBlock right) {
    return CodeBlock.of("($L | $L)", left, right);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  CodeBlock shl(CodeBlock left, CodeBlock right) {
    return CodeBlock.of("($L << $L)", left, right);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  CodeBlock eq(CodeBlock left, CodeBlock right) {
    return CodeBlock.of("($L == $L)", left, right);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TypeName getTypeName() {
    return TypeName.LONG;
  }
}
