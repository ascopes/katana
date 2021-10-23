package io.ascopes.katana.ap.introspection;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

/**
 * Search processor used to discover interfaces that have a specific annotation type applied to
 * them.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class InterfaceSearcher {
  private final ProcessingEnvironment processingEnv;

  public InterfaceSearcher(ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
  }

  public Stream<TypeElement> findAnnotatedInterfacesFor(
      RoundEnvironment roundEnv,
      TypeElement annotationType
  ) {
    Stream.Builder<TypeElement> interfaces = Stream.builder();

    for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(annotationType)) {
      tryUpcastAnnotatedType(annotationType, annotatedElement)
          .ifPresent(interfaces);
    }

    return interfaces.build();
  }

  private Optional<TypeElement> tryUpcastAnnotatedType(
      TypeElement annotationType,
      Element annotatedType
  ) {
    if (annotatedType.getKind() != ElementKind.INTERFACE) {
      failNotAnInterface(annotationType, annotatedType);
      return Optional.empty();
    }

    // I believe as of now we can assume that the element is a TypeElement, but let's just add
    // a check in to prevent future regressions and just in case I have missed anything.
    if (!(annotatedType instanceof TypeElement)) {
      failNotATypeElement(annotatedType);
      return Optional.empty();
    }

    return Optional
        .of(annotatedType)
        .map(TypeElement.class::cast);
  }

  private void failNotAnInterface(TypeElement annotationType, Element annotatedElement) {
    AnnotationMirror annotationMirror = ElementUtils
        .fetchAnnotationMirrorFor(this.processingEnv.getTypeUtils(), annotatedElement, annotationType);

    String message = "Annotation @"
        + annotationType.getQualifiedName()
        + " can only be applied to a regular interface. Use with "
        + annotatedElement.getKind().name().toLowerCase(Locale.ROOT).replace('_', ' ')
        + " is disallowed.";

    this.processingEnv
        .getMessager()
        .printMessage(
            Kind.ERROR,
            message,
            annotatedElement,
            annotationMirror
        );
  }

  private void failNotATypeElement(Element annotatedElement) {
    this.processingEnv
        .getMessager()
        .printMessage(
            Kind.ERROR,
            "Expected a TypeElement, got " + annotatedElement.getClass().getCanonicalName(),
            annotatedElement
        );
  }
}
