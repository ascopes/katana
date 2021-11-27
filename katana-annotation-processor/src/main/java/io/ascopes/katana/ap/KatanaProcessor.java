package io.ascopes.katana.ap;

import com.squareup.javapoet.JavaFile;
import io.ascopes.katana.annotations.ImmutableModel;
import io.ascopes.katana.annotations.MutableModel;
import io.ascopes.katana.ap.codegen.JavaFileWriter;
import io.ascopes.katana.ap.codegen.JavaModelFactory;
import io.ascopes.katana.ap.descriptors.AttributeFactory;
import io.ascopes.katana.ap.descriptors.AttributeFeatureInclusionManager;
import io.ascopes.katana.ap.descriptors.InterfaceSearcher;
import io.ascopes.katana.ap.descriptors.MethodClassificationFactory;
import io.ascopes.katana.ap.descriptors.Model;
import io.ascopes.katana.ap.descriptors.ModelFactory;
import io.ascopes.katana.ap.logging.Diagnostics;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.settings.SettingsResolver;
import io.ascopes.katana.ap.utils.Result;
import java.util.Collections;
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

    SettingsResolver settingsResolver = new SettingsResolver(
        this.processingEnv.getElementUtils(),
        this.processingEnv.getTypeUtils()
    );

    MethodClassificationFactory methodClassifier = new MethodClassificationFactory(
        diagnostics,
        this.processingEnv.getElementUtils(),
        this.processingEnv.getTypeUtils()
    );

    InterfaceSearcher interfaceSearcher = new InterfaceSearcher(diagnostics);

    AttributeFeatureInclusionManager attributeFeatureInclusionManager =
        new AttributeFeatureInclusionManager(
            diagnostics,
            this.processingEnv.getElementUtils()
        );

    AttributeFactory attributeFactory = new AttributeFactory(
        attributeFeatureInclusionManager,
        this.processingEnv.getElementUtils()
    );

    ModelFactory modelFactory = new ModelFactory(
        settingsResolver,
        methodClassifier,
        attributeFactory,
        diagnostics,
        this.processingEnv.getElementUtils()
    );

    JavaModelFactory javaModelFactory = new JavaModelFactory();

    JavaFileWriter javaFileWriter = new JavaFileWriter(
        this.processingEnv.getFiler(),
        diagnostics
    );

    this.interfaceSearcher = interfaceSearcher;
    this.modelFactory = modelFactory;
    this.javaModelFactory = javaModelFactory;
    this.javaFileWriter = javaFileWriter;
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
        .forEach(result -> {
          if (result.isNotOk()) {
            failed.incrementAndGet();
          } else if (result.isIgnored()) {
            throw new RuntimeException("Ignored result came from somewhere. This is a bug!");
          }
          processed.incrementAndGet();
        });

    long delta = System.nanoTime() - start;
    double rate = (double) delta / processed.get();

    this.logger.info(
        "Processed {} in {} ({}) ({} failures)",
        processed.get() == 1 ? "1 model definition" : processed.get() + " model definitions",
        String.format("~%.1fms", delta / 1_000_000.0),
        String.format("~%.1f per second", rate / 1_000_000.0),
        failed.get()
    );

    return true;
  }

  private Stream<Result<Model>> generateModelsForAnnotation(
      TypeElement annotationType,
      RoundEnvironment roundEnv
  ) {
    return this
        .interfaceSearcher
        .findAnnotatedInterfacesFor(annotationType, roundEnv)
        .map(interfaceType -> this.modelFactory.create(annotationType, interfaceType));
  }

  private Result<Void> buildJavaFile(Model model) {
    JavaFile file = this.javaModelFactory.create(model);
    return this.javaFileWriter.writeOutFile(model.getQualifiedName(), file);
  }
}
