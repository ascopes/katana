package io.ascopes.katana.ap.codegen;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import io.ascopes.katana.ap.analysis.Attribute;
import java.util.Optional;

/**
 * Initialization tracker base interface. This defines the contract for a type that consumes a
 * collection of required attributes (e.g. on a builder) and provides the ability to track whether
 * attributes are explicitly initialized or not using some internal implementation detail. The
 * expressions to define and manipulate these checks are provided by the methods on this interface.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
interface InitTracker {

  /**
   * Get the tracker variable type name.
   *
   * @return the type to initialize the tracking variable with.
   */
  TypeName getTrackerType();

  /**
   * Determine if the field holding the tracking variable can be marked as final or not.
   *
   * @return true if the variable can be final, false if it cannot.
   */
  boolean isTrackingVariableFinal();

  /**
   * Get an expression to assign the initial value to the tracking value with.
   *
   * @param trackingVariable the expression to get the reference to the tracking variable.
   * @return the expression in a code block. The expression is always atomic (meaning complex
   *     expressions where operator precedence takes place will be wrapped in parentheses).
   */
  CodeBlock initializeTracker(CodeBlock trackingVariable);

  /**
   * Get a statement that updates the tracking variable to indicate a given attribute has been
   * initialized.
   *
   * @param trackingVariable the expression to get the reference to the tracking variable.
   * @param attribute        the attribute to mark
   * @return the statement in a code block, if the attribute is one that is tracked. Otherwise, an
   *     empty optional.
   */
  Optional<CodeBlock> markAttributeInitialized(CodeBlock trackingVariable, Attribute attribute);

  /**
   * Get a boolean expression to check if a given attribute is not initialized.
   *
   * @param trackingVariable the expression to get the reference to the tracking variable.
   * @return the expression in a code block, if the attribute is one that is tracked. Otherwise, an
   *     empty optional. The expression is always atomic (meaning complex conditions where operator
   *     precedence takes place will be wrapped in parentheses).
   */
  Optional<CodeBlock> isAttributeUninitialized(CodeBlock trackingVariable, Attribute attribute);

  /**
   * Get a boolean expression to determine if there are any uninitialized attributes.
   *
   * @param trackingVariable the expression to get the reference to the tracking variable.
   * @return the expression in a code block. The expression is always atomic (meaning complex
   *     conditions where operator precedence takes place will be wrapped in parentheses).
   */
  CodeBlock isAnyUninitialized(CodeBlock trackingVariable);
}
