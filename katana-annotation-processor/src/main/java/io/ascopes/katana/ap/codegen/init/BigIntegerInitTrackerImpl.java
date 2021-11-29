package io.ascopes.katana.ap.codegen.init;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import io.ascopes.katana.ap.descriptors.Attribute;
import java.math.BigInteger;
import java.util.SortedSet;

/**
 * Tracker for at least 64 attributes, should any need ever arise. This will use a {@link
 * java.math.BigInteger} to store the state.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
final class BigIntegerInitTrackerImpl extends AbstractInitTracker {

  /**
   * Initialize this tracker.
   *
   * @param attributeSet      the attribute set of required attributes.
   * @param trackingFieldName the tracking field name.
   */
  BigIntegerInitTrackerImpl(SortedSet<Attribute> attributeSet, String trackingFieldName) {
    super(attributeSet, trackingFieldName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  CodeBlock cast(int value) {
    switch (value) {
      case 0:
        return CodeBlock.of("$T.ZERO", BigInteger.class);
      case 1:
        return CodeBlock.of("$T.ONE", BigInteger.class);
      default:
        return CodeBlock.of("$T.valueOf($L)", BigInteger.class, (long) value);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  CodeBlock sub(CodeBlock left, CodeBlock right) {
    return CodeBlock.of("$L.sub($L)", left, right);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  CodeBlock and(CodeBlock left, CodeBlock right) {
    return CodeBlock.of("$L.and($L)", left, right);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  CodeBlock or(CodeBlock left, CodeBlock right) {
    return CodeBlock.of("$L.or($L)", left, right);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  CodeBlock shl(CodeBlock left, CodeBlock right) {
    return CodeBlock.of("$L.shiftLeft($L)", left, right);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  CodeBlock eq(CodeBlock left, CodeBlock right) {
    return CodeBlock.of("$L.equals($L)", left, right);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TypeName getTypeName() {
    return ClassName.get(BigInteger.class);
  }
}
