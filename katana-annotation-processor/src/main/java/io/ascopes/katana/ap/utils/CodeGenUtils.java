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

/**
 * Code generation helpers.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public abstract class CodeGenUtils {

  private static final AnnotationSpec OVERRIDE = AnnotationSpec.builder(Override.class).build();

  public static Modifier[] modifiers(Visibility visibility) {
    switch (visibility) {
      case PRIVATE:
        return new Modifier[]{Modifier.PRIVATE};
      case PACKAGE_PRIVATE:
        return new Modifier[0];
      case PROTECTED:
        return new Modifier[]{Modifier.PROTECTED};
      case PUBLIC:
        return new Modifier[]{Modifier.PUBLIC};
      default:
        throw new IllegalArgumentException("Unknown visibility modifier " + visibility);
    }
  }

  public static AnnotationSpec override() {
    return OVERRIDE;
  }

  public static AnnotationSpec copyDeprecatedFrom(AnnotationMirror annotationMirror) {
    return AnnotationSpec.get(annotationMirror);
  }

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

  private CodeGenUtils() {
    throw new UnsupportedOperationException("static-only class");
  }
}
