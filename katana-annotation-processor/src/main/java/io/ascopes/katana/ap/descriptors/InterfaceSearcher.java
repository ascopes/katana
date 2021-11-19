package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.utils.AnnotationUtils;
import io.ascopes.katana.ap.utils.DiagnosticTemplates;
import io.ascopes.katana.ap.utils.Result;
import java.util.stream.Stream;
import javax.annotation.processing.Messager;
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

  private final DiagnosticTemplates diagnosticTemplates;
  private final Messager messager;

  /**
   * @param diagnosticTemplates diagnostics templates to use.
   * @param messager            the messager to use.
   */
  public InterfaceSearcher(DiagnosticTemplates diagnosticTemplates, Messager messager) {
    this.diagnosticTemplates = diagnosticTemplates;
    this.messager = messager;
  }

  public Stream<TypeElement> findAnnotatedInterfacesFor(
      TypeElement annotationType,
      RoundEnvironment roundEnv
  ) {
    Stream.Builder<TypeElement> interfaces = Stream.builder();

    for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(annotationType)) {
      this.tryUpcastAnnotatedType(annotationType, annotatedElement)
          .ifOkThen(interfaces);
    }

    return interfaces.build();
  }

  private Result<TypeElement> tryUpcastAnnotatedType(
      TypeElement annotationType,
      Element annotatedType
  ) {
    if (annotatedType.getKind() == ElementKind.PACKAGE) {
      // We allow packages to be annotated but do not process them actively outside settings
      // resolution. Just ignore it.
      return Result.ignore();
    }

    if (annotatedType.getKind() != ElementKind.INTERFACE) {
      this.failNotAnInterface(annotationType, annotatedType);
      return Result.fail();
    }

    // I believe as of now we can assume that the element is a TypeElement, but let's just add
    // a check in to prevent future regressions and just in case I have missed anything.
    if (!(annotatedType instanceof TypeElement)) {
      this.failUnexpectedElement(annotationType, annotatedType);
      return Result.fail();
    }

    return Result
        .ok(annotatedType)
        .ifOkMap(TypeElement.class::cast);
  }

  private void failNotAnInterface(TypeElement annotationType, Element annotatedElement) {
    AnnotationMirror mirror = AnnotationUtils
        .findAnnotationMirror(annotatedElement, annotationType)
        .elseReturn(null);

    String message = this.diagnosticTemplates
        .template("notAnInterfaceOrPackage")
        .placeholder("annotationName", annotationType.getSimpleName())
        .placeholder("erroneousElementName", annotatedElement.getSimpleName())
        .placeholder("erroneousElementKind", annotatedElement.getKind())
        .build();

    this.messager.printMessage(Kind.ERROR, message, annotatedElement, mirror);
  }

  private void failUnexpectedElement(TypeElement annotationType, Element annotatedElement) {
    // This may be caused by the types in Javac changing in the future, so just give some useful
    // contextual info in case the user files a bugreport for us.
    AnnotationMirror mirror = AnnotationUtils
        .findAnnotationMirror(annotatedElement, annotationType)
        .elseReturn(null);

    String message = this.diagnosticTemplates
        .template("unexpectedElement")
        .placeholder("annotationName", annotationType.getSimpleName())
        .placeholder("erroneousElementName", annotatedElement.getSimpleName())
        .placeholder("erroneousElementKind", annotatedElement.getKind())
        .build();

    this.messager.printMessage(Kind.ERROR, message, annotatedElement, mirror);
  }
}
