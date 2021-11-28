package io.ascopes.katana.ap.utils;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeName;
import io.ascopes.katana.annotations.Generated;
import io.ascopes.katana.annotations.Visibility;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.checkerframework.common.util.report.qual.ReportCreation;
import org.checkerframework.common.util.report.qual.ReportInherit;

/**
 * Code generation helpers.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@ReportCreation
@ReportInherit
public final class CodeGenUtils {

  private CodeGenUtils() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Get an array of 0 or 1 visibility modifiers to pass to JavaPoet from the given visibility
   * flag.
   *
   * @param visibility the visibility flag.
   * @return an array of 0 or 1 visibility modifiers.
   */
  public static Modifier[] modifiers(Visibility visibility) {
    switch (visibility) {
      case PRIVATE:
        return new Modifier[]{Modifier.PRIVATE};
      case PROTECTED:
        return new Modifier[]{Modifier.PROTECTED};
      case PUBLIC:
        return new Modifier[]{Modifier.PUBLIC};

      // Skip modifiers on anything else, including package private.
      default:
        return new Modifier[0];
    }
  }

  /**
   * Generate an {@link Override} annotation spec.
   *
   * @return an {@link Override} annotation spec.
   */
  public static AnnotationSpec override() {
    return AnnotationSpec.builder(Override.class).build();
  }

  /**
   * Copy an annotation mirror that contains a {@link Deprecated} annotation.
   *
   * @param annotationMirror the mirror containing the deprecation annotation.
   * @return an annotation spec that mirrors the deprecated annotation mirror.
   */
  public static AnnotationSpec copyDeprecatedFrom(AnnotationMirror annotationMirror) {
    return AnnotationSpec.get(annotationMirror);
  }

  /**
   * Create a {@link Generated} annotation spec to apply to generated models.
   *
   * @param modelInterface the model interface that the model was generated from.
   * @return an annotation spec for a {@link Generated} annotation.
   */
  public static AnnotationSpec generated(TypeElement modelInterface) {
    OffsetDateTime now = OffsetDateTime.now(Clock.systemDefaultZone());
    String nowString = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(now);
    return AnnotationSpec
        .builder(Generated.class)
        .addMember("name", "$S", "Katana Annotation Processor")
        .addMember("date", "$S", nowString)
        .addMember("from", "$T.class", TypeName.get(modelInterface.asType()))
        .build();
  }
}
