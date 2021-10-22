package io.ascopes.katana.ap.processing;

import io.ascopes.katana.annotations.ImmutableModel;
import io.ascopes.katana.annotations.MutableModel;
import io.ascopes.katana.ap.descriptors.Model;
import io.ascopes.katana.ap.utils.Functors;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

/**
 * Pipeline for generating models from annotated compiler data.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class ProcessorPipeline {
  private static final Class<MutableModel> MUTABLE_ANNOTATION = MutableModel.class;
  private static final Class<ImmutableModel> IMMUTABLE_ANNOTATION = ImmutableModel.class;

  private final RoundEnvironment roundEnv;
  private final InterfaceSearcher interfaceSearcher;
  private final DescriptorFactory descriptorFactory;

  /**
   * @param processingEnv the processing environment to use.
   * @param roundEnv      the round environment to use.
   */
  public ProcessorPipeline(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv) {
    this.roundEnv = roundEnv;
    this.interfaceSearcher = new InterfaceSearcher(processingEnv);
    this.descriptorFactory = new DescriptorFactory(processingEnv);
  }

  /**
   * @return the supported annotation names.
   */
  public static Stream<String> supportedAnnotationNames() {
    return Stream.of(MUTABLE_ANNOTATION, IMMUTABLE_ANNOTATION)
        .map(Class::getCanonicalName);
  }

  /**
   * @return the maximum source version that is supported by this processor.
   */
  public static SourceVersion maxSupportedSourceVersion() {
    // We support up to JDK-17 at the time of writing, but we do not have access to that constant,
    // so just bodge in the current compiler version and hope for the best.
    return SourceVersion.latestSupported();
  }

  /**
   * Perform the processing and write out the generated sources.
   *
   * @param annotationTypes the annotation types to consider.
   */
  public void process(Stream<? extends TypeElement> annotationTypes) {
    annotationTypes
        .flatMap(this::generateAllDescriptors)
        .forEach(System.out::println);
  }

  private Stream<Model> generateAllDescriptors(TypeElement annotationType) {
    return this.interfaceSearcher
        .findAnnotatedInterfacesFor(this.roundEnv, annotationType)
        .map(interfaceElement -> generateSingleDescriptor(annotationType, interfaceElement))
        .flatMap(Functors.removeEmpties());
  }

  private Optional<Model> generateSingleDescriptor(TypeElement annotationType, TypeElement annotatedElement) {
    Name annotationName = annotationType.getQualifiedName();

    if (annotationName.contentEquals(MUTABLE_ANNOTATION.getCanonicalName())) {
      MutableModel annotation = annotatedElement.getAnnotation(MUTABLE_ANNOTATION);
      return this.descriptorFactory.buildDescriptorFor(annotation, annotatedElement);
    }

    if (annotationName.contentEquals(IMMUTABLE_ANNOTATION.getCanonicalName())) {
      ImmutableModel annotation = annotatedElement.getAnnotation(IMMUTABLE_ANNOTATION);
      return this.descriptorFactory.buildDescriptorFor(annotation, annotatedElement);
    }

    return Optional.empty();
  }
}
