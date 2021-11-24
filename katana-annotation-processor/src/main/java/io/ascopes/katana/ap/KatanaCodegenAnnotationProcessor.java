package io.ascopes.katana.ap;

import com.squareup.javapoet.JavaFile;
import io.ascopes.katana.ap.codegen.JavaFileWriter;
import io.ascopes.katana.ap.codegen.SourceFileFactory;
import io.ascopes.katana.ap.descriptors.AttributeFactory;
import io.ascopes.katana.ap.descriptors.AttributeFeatureInclusionManager;
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
        this.processingEnv.getElementUtils(),
        this.processingEnv.getTypeUtils()
    );

    InterfaceSearcher interfaceSearcher = new InterfaceSearcher(
        diagnosticTemplates,
        this.processingEnv.getMessager()
    );

    AttributeFeatureInclusionManager attributeFeatureInclusionManager =
        new AttributeFeatureInclusionManager(
            diagnosticTemplates,
            this.processingEnv.getElementUtils(),
            this.processingEnv.getMessager()
        );

    AttributeFactory attributeFactory = new AttributeFactory(
        attributeFeatureInclusionManager,
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

    AtomicInteger processed = new AtomicInteger();
    AtomicInteger failed = new AtomicInteger();
    long start = System.nanoTime();

    annotationTypes
        .stream()
        .flatMap(annotationType -> this.generateModelsForAnnotation(annotationType, roundEnv))
        .map(model -> model.ifOkFlatMap(this::buildJavaFile))
        .map(file -> file.ifOkFlatMap(this::writeJavaFile))
        .forEach(result -> {
          if (result.isNotOk()) {
            failed.incrementAndGet();
          } else if (result.isIgnored()) {
            throw new RuntimeException("Ignored result came from somewhere. This is a bug!");
          }
          processed.incrementAndGet();
        });

    long delta = System.nanoTime() - start;

    this.logger.info(
        "Processed {} model definitions in approx {}ms ({} failures)",
        processed.get(),
        Math.round(delta / 1_000_000.0),
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
        .map(interfaceType -> this.modelFactory.buildFor(annotationType, interfaceType));
  }

  private Result<JavaFile> buildJavaFile(Model model) {
    return Result.ok(this.sourceFileFactory.buildJavaFileFrom(model));
  }

  private Result<Void> writeJavaFile(JavaFile javaFile) {
    return this
        .javaFileWriter
        .writeOutFile(javaFile);
  }
}
