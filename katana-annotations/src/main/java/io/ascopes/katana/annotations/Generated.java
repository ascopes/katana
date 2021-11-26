package io.ascopes.katana.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A generated annotation to use, since OpenJDK have made it impossible to have a consistent
 * annotation for this between JDK 8 and JDK 9.
 * <p>
 * Generated Katana classes will use this annotation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
@Target(ElementType.TYPE)
public @interface Generated {

  /**
   * The name of the generator.
   *
   * @return the name of the generator.
   */
  String name();

  /**
   * Date when the source was generated. The date element must follow the ISO 8601 standard. For
   * example the date element would have the following value 2017-07-04T12:08:56.235-0700 which
   * represents 2017-07-04 12:08:56 local time in the U.S. Pacific Time timezone.
   *
   * @return The date the source was generated
   */
  String date();

  /**
   * @return the interface that the model was created from.
   */
  Class<?> from();
}
