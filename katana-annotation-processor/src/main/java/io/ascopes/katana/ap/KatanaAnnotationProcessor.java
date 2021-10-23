package io.ascopes.katana.ap;

import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

/**
 * Entrypoint for the Katana annotation processor.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class KatanaAnnotationProcessor extends AbstractProcessor {
  /**
   * @return the supported annotation types.
   */
  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return ProcessorPipeline
        .supportedAnnotationNames()
        .collect(Collectors.toSet());
  }

  /**
   * @return the supported source version.
   */
  @Override
  public SourceVersion getSupportedSourceVersion() {
    return ProcessorPipeline.maxSupportedSourceVersion();
  }

  /**
   * Invoke the processing pipeline.
   *
   * @param annotationTypes the annotation types to check out.
   * @param roundEnv        the round environment.
   * @return {@code true}.
   */
  @Override
  public boolean process(Set<? extends TypeElement> annotationTypes, RoundEnvironment roundEnv) {
    if (!annotationTypes.isEmpty()) {
      new ProcessorPipeline(this.processingEnv, roundEnv)
          .process(annotationTypes.stream());
    }

    return true;
  }
}
