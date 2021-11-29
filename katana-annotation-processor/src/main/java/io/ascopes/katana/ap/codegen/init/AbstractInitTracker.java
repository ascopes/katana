package io.ascopes.katana.ap.codegen.init;

import com.squareup.javapoet.CodeBlock;
import io.ascopes.katana.ap.descriptors.Attribute;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import org.checkerframework.checker.optional.qual.MaybePresent;


/**
 * Base implementation for an initialization tracker.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
abstract class AbstractInitTracker implements InitTracker {

  private final Map<Attribute, CodeBlock> attributes;
  private final CodeBlock allMask;
  private final String trackingFieldName;

  /**
   * Initialize this abstract tracker.
   *
   * @param attributeSet      the required attributes to track.
   * @param trackingFieldName the tracking field name to use.
   */
  AbstractInitTracker(SortedSet<Attribute> attributeSet, String trackingFieldName) {
    Objects.requireNonNull(attributeSet);

    // Maintain the order of the input, just to keep results deterministic.
    this.attributes = new LinkedHashMap<>();

    CodeBlock one = this.cast(1);

    int offset = 0;
    for (Attribute attribute : attributeSet) {
      Objects.requireNonNull(attribute);
      CodeBlock bitflag = this.shl(one, this.cast(offset++));
      this.attributes.put(attribute, bitflag);
    }

    this.allMask = this.sub(this.shl(one, this.cast(offset)), one);
    this.trackingFieldName = Objects.requireNonNull(trackingFieldName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean isEmpty() {
    return this.attributes.isEmpty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getFieldName() {
    return this.trackingFieldName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @MaybePresent
  public final Optional<CodeBlock> getInitializedExpr(String scope, Attribute attribute) {
    Objects.requireNonNull(scope);
    Objects.requireNonNull(attribute);
    CodeBlock trackingVariable = this.getTrackingVariableReference(scope);

    return this
        .getLiteralFor(attribute)
        .map(mask -> this.eq(mask, this.and(trackingVariable, mask)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @MaybePresent
  public final Optional<CodeBlock> getUninitializedExpr(String scope, Attribute attribute) {
    Objects.requireNonNull(scope);
    Objects.requireNonNull(attribute);
    CodeBlock trackingVariable = this.getTrackingVariableReference(scope);

    return this
        .getLiteralFor(attribute)
        .map(mask -> this.eq(trackingVariable, this.or(trackingVariable, mask)));
  }

  @Override
  @MaybePresent
  public Optional<CodeBlock> getUpdateInitializedExpr(String scope, Attribute attribute) {
    Objects.requireNonNull(scope);
    Objects.requireNonNull(attribute);
    CodeBlock trackingVariable = this.getTrackingVariableReference(scope);

    return this
        .getLiteralFor(attribute)
        .map(mask -> this.assign(trackingVariable, this.or(trackingVariable, mask)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final CodeBlock getAnyUninitializedExpr(String scope) {
    Objects.requireNonNull(scope);
    CodeBlock trackingVariable = this.getTrackingVariableReference(scope);
    return this.eq(trackingVariable, this.or(trackingVariable, this.allMask));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final CodeBlock getTrackingVariableInitialValue() {
    return this.cast(0);
  }

  /**
   * Create a literal value for the desired tracking type from a given int.
   *
   * @param value the value to cast.
   * @return the resultant code block.
   */
  abstract CodeBlock cast(int value);

  /**
   * Subtraction {@code -} operator.
   *
   * <p>Returned values should always be surrounded by parenthesis.
   *
   * @param left  the left oprand.
   * @param right the right oprand.
   * @return the resultant code block.
   */
  abstract CodeBlock sub(CodeBlock left, CodeBlock right);

  /**
   * And {@code &amp;} operator.
   *
   * <p>Returned values should always be surrounded by parenthesis.
   *
   * @param left  the left oprand.
   * @param right the right oprand.
   * @return a code block representing the expression of the bitwise-and of the left and right
   *     oprands.
   */
  abstract CodeBlock and(CodeBlock left, CodeBlock right);

  /**
   * Or {@code |} operator.
   *
   * <p>Returned values should always be surrounded by parenthesis.
   *
   * @param left  the left oprand.
   * @param right the right oprand.
   * @return a code block representing the expression of the bitwise-or of the left and right
   *     oprands.
   */
  abstract CodeBlock or(CodeBlock left, CodeBlock right);

  /**
   * Left-bitshift {@code &lt;&lt;} operator.
   *
   * <p>Returned values should always be surrounded by parenthesis.
   *
   * @param left  the left oprand.
   * @param right the right oprand.
   * @return a code block representing the expression of the left bitshift of the left and right
   *     oprands.
   */
  abstract CodeBlock shl(CodeBlock left, CodeBlock right);

  /**
   * Equality {@code ==} operator.
   *
   * <p>This is expected to be equivalent to {@link Object#equals(Object)} instead of a literal
   * {@code ==} operation, when reference types are being compared.
   *
   * <p>Returned values should always be surrounded by parenthesis.
   *
   * @param left  the left oprand.
   * @param right the right oprand.
   * @return a code block representing the expression of the equality of the left and right oprands.
   */
  abstract CodeBlock eq(CodeBlock left, CodeBlock right);

  private CodeBlock assign(CodeBlock variable, CodeBlock expression) {
    return CodeBlock.of("$L = $L", variable, expression);
  }

  private Optional<CodeBlock> getLiteralFor(Attribute attribute) {
    return Optional.ofNullable(this.attributes.get(attribute));
  }

  private CodeBlock getTrackingVariableReference(String scope) {
    return CodeBlock.of("$L.$N", scope, this.trackingFieldName);
  }
}
