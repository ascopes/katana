package io.ascopes.katana.ap.utils;

import org.checkerframework.checker.mustcall.qual.MustCall;

/**
 * Functional base for any builder types.
 *
 * @param <T> the result type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@MustCall("build")
public interface ObjectBuilder<T> {

  T build();
}
