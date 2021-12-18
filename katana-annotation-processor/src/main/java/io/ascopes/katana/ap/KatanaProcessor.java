/*
 * Copyright (C) 2021 Ashley Scopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ascopes.katana.ap;

import io.ascopes.katana.annotations.ImmutableModel;
import io.ascopes.katana.annotations.MutableModel;
import io.ascopes.katana.ap.files.CompilationUnitWriter;
import io.ascopes.katana.ap.logging.Diagnostics;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.logging.LoggingLevel;
import io.ascopes.katana.ap.types.DataClassSourceFactory;
import io.ascopes.katana.ap.types.InterfaceSearcher;
import io.ascopes.katana.ap.types.ModelDescriptor;
import io.ascopes.katana.ap.types.ModelDescriptorFactory;
import io.ascopes.katana.ap.utils.Result;
import io.ascopes.katana.ap.utils.TimerUtils;
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
  private @MonotonicNonNull ModelDescriptorFactory modelDescriptorFactory;
  private @MonotonicNonNull DataClassSourceFactory dataClassSourceFactory;
  private @MonotonicNonNull CompilationUnitWriter compilationUnitWriter;
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

    this.modelDescriptorFactory = new ModelDescriptorFactory(
        diagnostics,
        this.processingEnv.getElementUtils(),
        this.processingEnv.getTypeUtils()
    );

    this.dataClassSourceFactory = new DataClassSourceFactory();

    this.compilationUnitWriter = new CompilationUnitWriter(
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

    double duration = TimerUtils.timing(() -> annotationTypes
        .stream()
        .flatMap(annotationType -> this.generateModelsForAnnotation(annotationType, roundEnv))
        .map(model -> model.ifOkMap(this.dataClassSourceFactory::create))
        .map(javaFile -> javaFile.ifOkFlatMap(this.compilationUnitWriter::write))
        .forEach(result -> this.handleResult(result, processed, failed)));

    this.logger.info(
        "Processed {} in {} ({}) ({} failures - {})",
        processed.get() == 1 ? "1 model definition" : processed.get() + " model definitions",
        String.format("~%.3fs", duration),
        String.format("~%.3f per second", processed.get() / duration),
        failed.get(),
        String.format("~%.3f per second", failed.get() > 0 ? failed.get() / duration : 0)
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

  private Stream<Result<ModelDescriptor>> generateModelsForAnnotation(
      TypeElement annotationType,
      RoundEnvironment roundEnv
  ) {
    return this
        .interfaceSearcher
        .findAllInterfacesWithAnnotation(annotationType, roundEnv)
        .map(interfaceType -> this.modelDescriptorFactory.create(annotationType, interfaceType));
  }
}
