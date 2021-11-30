package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.logging.Diagnostics;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.utils.AnnotationUtils;
import java.util.Optional;
import java.util.stream.Stream;
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
public final class InterfaceSearcher {

  private final Diagnostics diagnostics;
  private final Logger logger;

  /**
   * Initialize this interface searcher.
   *
   * @param diagnostics the diagnostics to use.
   */
  public InterfaceSearcher(Diagnostics diagnostics) {
    this.diagnostics = diagnostics;
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  /**
   * Find all model interfaces for a given model annotation type in the given round environment.
   *
   * @param annotationType the model annotation type to look for.
   * @param roundEnv       the round environment.
   * @return a stream of all discovered interfaces.
   */
  public Stream<TypeElement> findAllInterfacesWithAnnotation(
      TypeElement annotationType,
      RoundEnvironment roundEnv
  ) {
    Stream.Builder<TypeElement> interfaces = Stream.builder();

    for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(annotationType)) {
      this.tryUpcastAnnotatedType(annotationType, annotatedElement)
          .ifPresent(interfaceType -> {
            this.logger.trace(
                "Found interface {} matching or ascending from {}-annotated interface",
                interfaceType.getQualifiedName(),
                annotationType.getQualifiedName()
            );

            interfaces.accept(interfaceType);
          });
    }

    return interfaces.build();
  }

  private Optional<TypeElement> tryUpcastAnnotatedType(
      TypeElement annotationType,
      Element annotatedType
  ) {
    if (annotatedType.getKind() == ElementKind.PACKAGE) {
      // We allow packages to be annotated but do not process them actively outside settings
      // resolution. Just ignore it.
      return Optional.empty();
    }

    if (annotatedType.getKind() != ElementKind.INTERFACE) {
      this.failNotAnInterface(annotationType, annotatedType);
      return Optional.empty();
    }

    // I believe as of now we can assume that the element is a TypeElement, but let's just add
    // a check in to prevent future regressions and just in case I have missed anything.
    if (!(annotatedType instanceof TypeElement)) {
      this.failUnexpectedElement(annotationType, annotatedType);
      return Optional.empty();
    }

    return Optional.of((TypeElement) annotatedType);
  }

  private void failNotAnInterface(TypeElement annotationType, Element annotatedElement) {
    AnnotationMirror mirror = AnnotationUtils
        .findAnnotationMirror(annotatedElement, annotationType)
        .orElse(null);

    this.diagnostics
        .builder()
        .kind(Kind.ERROR)
        .element(annotatedElement)
        .annotationMirror(mirror)
        .template("notAnInterfaceOrPackage")
        .param("annotationName", annotationType.getSimpleName())
        .param("erroneousElementName", annotatedElement.getSimpleName())
        .param("erroneousElementKind", annotatedElement.getKind())
        .log();
  }

  private void failUnexpectedElement(TypeElement annotationType, Element annotatedElement) {
    // This may be caused by the types in Javac changing in the future, so just give some useful
    // contextual info in case the user files a bugreport for us.
    AnnotationMirror mirror = AnnotationUtils
        .findAnnotationMirror(annotatedElement, annotationType)
        .orElse(null);

    this.diagnostics
        .builder()
        .kind(Kind.ERROR)
        .element(annotatedElement)
        .annotationMirror(mirror)
        .template("unexpectedElement")
        .param("annotationName", annotationType.getSimpleName())
        .param("erroneousElementName", annotatedElement.getSimpleName())
        .param("erroneousElementKind", annotatedElement.getKind())
        .log();
  }
}
