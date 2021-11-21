package io.ascopes.katana.ap;

import com.squareup.javapoet.JavaFile;
import io.ascopes.katana.ap.codegen.JavaFileWriter;
import io.ascopes.katana.ap.codegen.SourceFileFactory;
import io.ascopes.katana.ap.descriptors.AttributeFactory;
import io.ascopes.katana.ap.descriptors.InterfaceSearcher;
import io.ascopes.katana.ap.descriptors.MethodClassifier;
import io.ascopes.katana.ap.descriptors.Model;
import io.ascopes.katana.ap.descriptors.ModelFactory;
import io.ascopes.katana.ap.settings.SettingsResolver;
import io.ascopes.katana.ap.utils.DiagnosticTemplates;
import io.ascopes.katana.ap.utils.Result;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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
  private @MonotonicNonNull SourceFileFactory sourceFileFactory;
  private @MonotonicNonNull JavaFileWriter javaFileWriter;

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

    SourceFileFactory sourceFileFactory = new SourceFileFactory();

    JavaFileWriter javaFileWriter = new JavaFileWriter(
        this.processingEnv.getFiler(),
        this.processingEnv.getMessager(),
        diagnosticTemplates
    );

    this.interfaceSearcher = interfaceSearcher;
    this.modelFactory = modelFactory;
    this.sourceFileFactory = sourceFileFactory;
    this.javaFileWriter = javaFileWriter;
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

    AtomicInteger successes = new AtomicInteger();
    AtomicInteger failures = new AtomicInteger();
    long start = System.nanoTime();

    annotationTypes
        .stream()
        .flatMap(annotationType -> this.generateModelsForAnnotation(annotationType, roundEnv))
        .map(model -> model.ifOkFlatMap(this::buildJavaFile))
        .map(file -> file.ifOkFlatMap(this::writeJavaFile))
        .forEach(result -> {
          if (result.isOk()) {
            successes.incrementAndGet();
          } else if (roundEnv.errorRaised()) {
            failures.incrementAndGet();
          } else {
            // Should never happen.
            throw new RuntimeException("Error raised but nothing was logged. This is a bug!");
          }
        });

    long delta = System.nanoTime() - start;

    this.logger.info(
        "Had {} successful results and {} failures in approx {}ms",
        successes,
        failures,
        Math.round(delta / 1_000_000.0)
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
        .map(interfaceType -> this.modelFactory.buildFor(annotationType, interfaceType));
  }

  private Result<JavaFile> buildJavaFile(Model model) {
    return this
        .sourceFileFactory
        .buildJavaFileFrom(model);
  }

  private Result<Void> writeJavaFile(JavaFile javaFile) {
    return this
        .javaFileWriter
        .writeOutFile(javaFile);
  }
}
