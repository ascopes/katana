package io.ascopes.katana.ap.processing;

import io.ascopes.katana.annotations.Settings;
import io.ascopes.katana.ap.descriptors.SettingsCollection;
import io.ascopes.katana.ap.utils.PackageIterator;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * Resolver for processor settings.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class SettingsResolver {
  private final ProcessingEnvironment processingEnv;

  public SettingsResolver(ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
  }

  public SettingsCollection parseSettings(Settings annotationSettings, TypeElement interfaceElement) {
    throw new UnsupportedOperationException();
  }

  private Stream<Settings> findAllInterfaceSettings(TypeElement interfaceElement) {
    throw new UnsupportedOperationException();
  }

  private Stream<Settings> findAllPackageSettings(TypeElement interfaceElement) {
    return new PackageIterator(this.processingEnv.getElementUtils(), interfaceElement)
        .toStream()
        .map(pkg -> pkg.getAnnotation(Settings.class))
        .filter(Objects::nonNull);
  }
}
