package io.ascopes.katana.spi.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation to mark a class to be used as a {@link java.util.ServiceLoader} provider
 * implementation.
 * <p>
 * This instructs <code>katana-spi-annotation-processor</code> to generate a
 * <code>META-INF/services</code> file for the provided interface.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ServiceProvider {

  /**
   * The interface that the annotated class should be listed under in the {@link
   * java.util.ServiceLoader}.
   * <p>
   * It is an error if this is not a supertype of the annotated class.
   *
   * @return the interface type.
   */
  Class<?> value();
}
