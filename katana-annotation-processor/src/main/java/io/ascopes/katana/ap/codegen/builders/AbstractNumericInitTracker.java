package io.ascopes.katana.ap.codegen.builders;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import io.ascopes.katana.ap.descriptors.Attribute;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;

/**
 * Init tracker that uses numbers with bitfields.
 *
 * @param <N> the number type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
abstract class AbstractNumericInitTracker<N extends Number> implements InitTracker {

  private final Map<Attribute, CodeBlock> flags;
  private final CodeBlock zero;
  private final TypeName type;

  AbstractNumericInitTracker(
      SortedSet<Attribute> attributes,
      CodeBlock zero,
      Class<N> type
  ) {
    this.flags = new HashMap<>();
    this.zero = zero;

    int index = 0;
    for (Attribute attribute : attributes) {
      this.flags.put(attribute, this.getFlag(index++));
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
      Attribute attribute
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
      Attribute attribute
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
