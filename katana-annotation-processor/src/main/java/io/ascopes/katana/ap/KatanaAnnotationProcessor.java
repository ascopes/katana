package io.ascopes.katana.ap;

import io.ascopes.katana.annotations.ImmutableModel;
import io.ascopes.katana.annotations.MutableModel;
import io.ascopes.katana.ap.descriptors.AttributeFactory;
import io.ascopes.katana.ap.descriptors.InterfaceSearcher;
import io.ascopes.katana.ap.descriptors.MethodClassifier;
import io.ascopes.katana.ap.descriptors.Model;
import io.ascopes.katana.ap.descriptors.ModelFactory;
import io.ascopes.katana.ap.settings.SettingsResolver;
import io.ascopes.katana.ap.utils.DiagnosticTemplates;
import io.ascopes.katana.ap.utils.Functors;
import io.ascopes.katana.ap.utils.Logger;
import io.ascopes.katana.ap.utils.Logger.Level;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

/**
 * Entrypoint for the Katana annotation processor.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class KatanaAnnotationProcessor extends AbstractProcessor {

  private static final Class<MutableModel> MUTABLE_ANNOTATION = MutableModel.class;
  private static final Class<ImmutableModel> IMMUTABLE_ANNOTATION = ImmutableModel.class;
  private static final String LOGGING_LEVEL = "logging.level";

  private final Logger logger;

  private InterfaceSearcher interfaceSearcher;
  private ModelFactory modelFactory;

  /**
   * Pre-initialize the processor.
   */
  public KatanaAnnotationProcessor() {
    this.logger = new Logger();
  }

  @Override
  public Set<String> getSupportedOptions() {
    return Collections.singleton(LOGGING_LEVEL);
  }

  /**
   * @return the supported annotation types.
   */
  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Stream.of(MUTABLE_ANNOTATION, IMMUTABLE_ANNOTATION)
        .map(Class::getCanonicalName)
        .collect(Collectors.toSet());
  }

  /**
   * @return the supported source version.
   */
  @Override
  public SourceVersion getSupportedSourceVersion() {
    // We support up to JDK-17 at the time of writing, but we do not have access to that constant,
    // so just bodge in the current compiler version and hope for the best.
    return SourceVersion.latestSupported();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    // Init the loggers.
    Optional
        .ofNullable(processingEnv.getOptions().get(LOGGING_LEVEL))
        .map(Level::valueOf)
        .ifPresent(Logger::setGlobalLevel);

    DiagnosticTemplates diagnosticTemplates = new DiagnosticTemplates();

    SettingsResolver settingsResolver = new SettingsResolver(
        processingEnv.getElementUtils(),
        processingEnv.getTypeUtils()
    );

    MethodClassifier methodClassifier = new MethodClassifier(
        diagnosticTemplates,
        processingEnv.getMessager(),
        processingEnv.getTypeUtils()
    );

    InterfaceSearcher interfaceSearcher = new InterfaceSearcher(
        diagnosticTemplates,
        processingEnv.getMessager()
    );

    AttributeFactory attributeFactory = new AttributeFactory(
        diagnosticTemplates,
        processingEnv.getMessager(),
        processingEnv.getElementUtils()
    );

    ModelFactory modelFactory = new ModelFactory(
        settingsResolver,
        methodClassifier,
        attributeFactory,
        processingEnv.getMessager(),
        processingEnv.getElementUtils()
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
  public boolean process(Set<? extends TypeElement> annotationTypes, RoundEnvironment roundEnv) {
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
