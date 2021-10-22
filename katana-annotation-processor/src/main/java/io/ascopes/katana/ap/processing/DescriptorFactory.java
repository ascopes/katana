package io.ascopes.katana.ap.processing;

import io.ascopes.katana.annotations.ImmutableModel;
import io.ascopes.katana.annotations.MutableModel;
import io.ascopes.katana.annotations.Settings;
import io.ascopes.katana.ap.descriptors.Model;
import java.util.Optional;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * Mapper to read in the AST data from the compiler and produce meaningful information for
 * other components to follow to build the models later.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class DescriptorFactory {
  private final ProcessingEnvironment processingEnv;

  public DescriptorFactory(ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
  }

  public Optional<Model> buildDescriptorFor(
      ImmutableModel immutableModel,
      TypeElement annotatedElement
  ) {
    Model descriptor = new Model();
    descriptor.setMutable(false);
    descriptor.setModelInterface(annotatedElement);
    return buildDescriptorFor(descriptor, immutableModel.settings());
  }

  public Optional<Model> buildDescriptorFor(
      MutableModel mutableModel,
      TypeElement annotatedElement
  ) {
    Model descriptor = new Model();
    descriptor.setMutable(true);
    descriptor.setModelInterface(annotatedElement);
    return buildDescriptorFor(descriptor, mutableModel.settings());
  }

  private Optional<Model> buildDescriptorFor(
      Model descriptor,
      Settings annotationSettings
  ) {
    throw new UnsupportedOperationException();
  }
}
