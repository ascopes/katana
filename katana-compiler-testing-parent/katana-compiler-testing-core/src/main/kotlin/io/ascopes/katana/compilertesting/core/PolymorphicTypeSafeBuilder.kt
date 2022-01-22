package io.ascopes.katana.compilertesting.core

/**
 * Base for a builder-like abstract class that should be able to return references of its
 * implementation type.
 *
 * @param B the type of the class that implements this interface.
 * @author Ashley Scopes
 * @since 0.1.0
 */
abstract class PolymorphicTypeSafeBuilder<B>
internal constructor()
    where B : PolymorphicTypeSafeBuilder<B> {
  /**
   * Override for [apply] that returns the implementation type of whatever implements
   * this interface.
   *
   * @param operation the operation to perform.
   * @return this object, cast to [B].
   */
  @Suppress("UNCHECKED_CAST")
  protected fun apply(operation: B.() -> Unit): B {
    val self = this as B
    self.operation()
    return self
  }
}