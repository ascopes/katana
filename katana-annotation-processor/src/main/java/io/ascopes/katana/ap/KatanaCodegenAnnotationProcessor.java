package io.ascopes.katana.ap;

import io.ascopes.katana.ap.descriptors.AttributeFactory;
import io.ascopes.katana.ap.descriptors.InterfaceSearcher;
import io.ascopes.katana.ap.descriptors.MethodClassifier;
import io.ascopes.katana.ap.descriptors.Model;
import io.ascopes.katana.ap.descriptors.ModelFactory;
import io.ascopes.katana.ap.settings.SettingsResolver;
import io.ascopes.katana.ap.utils.DiagnosticTemplates;
import io.ascopes.katana.ap.utils.Functors;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

/**
 * Entrypoint for the Katana annotation processor.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class KatanaCodegenAnnotationProcessor extends AbstractKatanaAnnotationProcessor {

  private @MonotonicNonNull InterfaceSearcher interfaceSearcher;
  private @MonotonicNonNull ModelFactory modelFactory;

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doInit() {
    DiagnosticTemplates diagnosticTemplates = new DiagnosticTemplates();

    SettingsResolver settingsResolver = new SettingsResolver(
        this.processingEnv.getElementUtils(),
        this.processingEnv.getTypeUtils()
    );

    MethodClassifier methodClassifier = new MethodClassifier(
        diagnosticTemplates,
        this.processingEnv.getMessager(),
        this.processingEnv.getTypeUtils()
    );

    InterfaceSearcher interfaceSearcher = new InterfaceSearcher(
        diagnosticTemplates,
        this.processingEnv.getMessager()
    );

    AttributeFactory attributeFactory = new AttributeFactory(
        diagnosticTemplates,
        this.processingEnv.getMessager(),
        this.processingEnv.getElementUtils()
    );

    ModelFactory modelFactory = new ModelFactory(
        settingsResolver,
        methodClassifier,
        attributeFactory,
        this.processingEnv.getMessager(),
        this.processingEnv.getElementUtils()
    );

    this.interfaceSearcher = interfaceSearcher;
    this.modelFactory = modelFactory;
  }

  /**
   * Invoke the processing pipeline.
   *
   * @param annotationTypes the annotation types to check out.
   * @param roundEnv        the round environment.
   * @return {@code true} if the processor ran.
   */
  @Override
  public boolean process(
      Set<? extends TypeElement> annotationTypes,
      RoundEnvironment roundEnv
  ) {
    if (annotationTypes.isEmpty()) {
      // Don't do anything.
      return true;
    }

    this.logger.info("Running annotation processor");

    annotationTypes
        .stream()
        .flatMap(this.modelsFor(roundEnv))
        .forEach(model -> this.logger.info("Created model {}", model));

    return true;
  }

  private Function<TypeElement, Stream<Model>> modelsFor(RoundEnvironment roundEnv) {
    return annotationType -> this
        .interfaceSearcher
        .findAnnotatedInterfacesFor(annotationType, roundEnv)
        .map(annotatedElement -> this.modelFactory
            .buildFor(annotationType, annotatedElement))
        .flatMap(Functors.removeNonOkResults());
  }
}
