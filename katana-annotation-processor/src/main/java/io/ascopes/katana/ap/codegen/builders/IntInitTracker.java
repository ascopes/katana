package io.ascopes.katana.ap.codegen.builders;

import com.squareup.javapoet.CodeBlock;
import io.ascopes.katana.ap.descriptors.Attribute;
import java.util.SortedSet;

/**
 * Initialization tracker that uses integer bitfields.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
class IntInitTracker extends AbstractPrimitiveNumericInitTracker<Integer> {

  IntInitTracker(SortedSet<Attribute> attributes) {
    super(attributes, int.class);
  }

  @Override
  CodeBlock valueOf(long value) {
    return CodeBlock.of("0b$L", Integer.toString((int) value, 2));
  }
}
