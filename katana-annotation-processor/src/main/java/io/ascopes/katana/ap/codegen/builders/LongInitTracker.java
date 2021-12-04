package io.ascopes.katana.ap.codegen.builders;

import com.squareup.javapoet.CodeBlock;
import io.ascopes.katana.ap.descriptors.Attribute;
import java.util.SortedSet;

/**
 * Initialization tracker that uses long bitfields.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
class LongInitTracker extends AbstractPrimitiveNumericInitTracker<Long> {

  LongInitTracker(SortedSet<Attribute> attributes) {
    super(attributes, long.class);
  }

  @Override
  CodeBlock valueOf(long value) {
    return CodeBlock.of("0b$L", Long.toString(value, 2));
  }
}
