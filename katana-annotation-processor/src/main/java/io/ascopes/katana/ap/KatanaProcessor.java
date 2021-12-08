package io.ascopes.katana.ap;

import com.squareup.javapoet.JavaFile;
import io.ascopes.katana.annotations.ImmutableModel;
import io.ascopes.katana.annotations.MutableModel;
import io.ascopes.katana.ap.analysis.InterfaceSearcher;
import io.ascopes.katana.ap.analysis.Model;
import io.ascopes.katana.ap.analysis.ModelFactory;
import io.ascopes.katana.ap.codegen.JavaFileWriter;
import io.ascopes.katana.ap.codegen.JavaModelFactory;
import io.ascopes.katana.ap.logging.Diagnostics;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.logging.LoggingLevel;
import io.ascopes.katana.ap.utils.Result;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

/**
 * Entrypoint for the Katana annotation processor.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class KatanaProcessor extends AbstractProcessor {

  private static final Class<MutableModel> MUTABLE_ANNOTATION = MutableModel.class;
  private static final Class<ImmutableModel> IMMUTABLE_ANNOTATION = ImmutableModel.class;
  private static final String LOGGING_LEVEL = "logging.level";

  private @MonotonicNonNull InterfaceSearcher interfaceSearcher;
  private @MonotonicNonNull ModelFactory modelFactory;
  private @MonotonicNonNull JavaModelFactory javaModelFactory;
  private @MonotonicNonNull JavaFileWriter javaFileWriter;
  private @MonotonicNonNull Logger logger;

  @Override
  public Set<String> getSupportedOptions() {
    return Collections.singleton(LOGGING_LEVEL);
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Stream.of(MUTABLE_ANNOTATION, IMMUTABLE_ANNOTATION)
        .map(Class::getCanonicalName)
        .collect(Collectors.toSet());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    // We support up to JDK-17 at the time of writing, but we do not have access to that constant,
    // so just bodge in the current compiler version and hope for the best.
    return SourceVersion.latestSupported();
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    // Init the loggers.
    Optional
        .ofNullable(processingEnv.getOptions().get(LOGGING_LEVEL))
        .ifPresent(LoggerFactory::globalLevel);

    this.logger = LoggerFactory.loggerFor(this.getClass());

    Diagnostics diagnostics = new Diagnostics(
        this.processingEnv.getMessager()
    );

    this.interfaceSearcher = new InterfaceSearcher(diagnostics);

    this.modelFactory = new ModelFactory(
        diagnostics,
        this.processingEnv.getElementUtils(),
        this.processingEnv.getTypeUtils()
    );

    this.javaModelFactory = new JavaModelFactory();

    this.javaFileWriter = new JavaFileWriter(
        this.processingEnv.getFiler(),
        diagnostics
    );

  }

  @Override
  public Iterable<? extends Completion> getCompletions(
      Element element,
      AnnotationMirror annotation,
      ExecutableElement member,
      String userText
  ) {
    // TODO(ascopes): implement.
    return Collections.emptySet();
  }

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

    AtomicInteger processed = new AtomicInteger();
    AtomicInteger failed = new AtomicInteger();
    long start = System.nanoTime();

    annotationTypes
        .stream()
        .flatMap(annotationType -> this.generateModelsForAnnotation(annotationType, roundEnv))
        .map(model -> model.ifOkFlatMap(this::buildJavaFile))
        .forEach(result -> this.handleResult(result, processed, failed));

    double delta = (System.nanoTime() - start) / 1_000_000_000D;
    double rate = processed.get() / delta;

    this.logger.info(
        "Processed {} in {} ({}) ({} failures)",
        processed.get() == 1 ? "1 model definition" : processed.get() + " model definitions",
        String.format("~%.3fs", delta),
        String.format("~%.3f per second", rate),
        failed.get()
    );

    return true;
  }

  private void handleResult(
      Result<?> result,
      AtomicInteger processedCount,
      AtomicInteger failedCount
  ) {
    processedCount.incrementAndGet();

    if (!result.isFailed()) {
      this.logger.debug("Pass succeeded");
      return;
    }

    failedCount.incrementAndGet();

    if (!this.logger.isEnabled(LoggingLevel.DEBUG)) {
      this.logger.error(
          "Failed to create model: {} (enable debug logs for more info)",
          result.getErrorReason().orElse("No message")
      );
      return;
    }

    this.logger.error("Failed to create model: {}\n{}",
        result.getErrorReason().orElse("No message"),
        result.getErrorLocation()
            .map(Objects::toString)
            .orElse("No trace present")
    );

  }

  private Stream<Result<Model>> generateModelsForAnnotation(
      TypeElement annotationType,
      RoundEnvironment roundEnv
  ) {
    return this
        .interfaceSearcher
        .findAllInterfacesWithAnnotation(annotationType, roundEnv)
        .map(interfaceType -> this.modelFactory.create(annotationType, interfaceType));
  }

  private Result<Void> buildJavaFile(Model model) {
    JavaFile file = this.javaModelFactory.create(model);
    return this.javaFileWriter.writeOutFile(model.getQualifiedName().reflectionName(), file);
  }
}
