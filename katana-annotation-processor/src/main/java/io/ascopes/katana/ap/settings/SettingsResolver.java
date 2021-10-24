package io.ascopes.katana.ap.settings;

import io.ascopes.katana.annotations.Settings;
import io.ascopes.katana.ap.commons.Streams;
import io.ascopes.katana.ap.introspection.PackageIterator;
import io.ascopes.katana.ap.introspection.SuperInterfaceIterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.PackageElement;
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
    List<RawSettings> allSettingsEntries = this
        .findAllSettingsEntries(annotationSettings, interfaceElement)
        .collect(Collectors.toList());

    return null;
  }

  private Stream<RawSettings> findAllSettingsEntries(
      Settings annotationSettings,
      TypeElement interfaceElement
  ) {
    Stream<RawSettings> annotationEntries = Stream
        .of(new RawSettings("annotation", SettingLocation.ANNOTATION, annotationSettings));

    Stream<RawSettings> interfaceEntries =
        new SuperInterfaceIterator(this.processingEnv.getTypeUtils(), interfaceElement)
            .stream()
            .map(this::findSettingsOn)
            .flatMap(Streams.removeEmpties());

    Stream<RawSettings> packageEntries =
        new PackageIterator(this.processingEnv.getElementUtils(), interfaceElement)
            .stream()
            .map(this::findSettingsOn)
            .flatMap(Streams.removeEmpties());

    return Stream
        .of(annotationEntries, interfaceEntries, packageEntries)
        .flatMap(Streams.flatten());
  }

  private Optional<RawSettings> findSettingsOn(TypeElement typeElement) {
    return this.findSettingsOn(
        typeElement,
        typeElement.getQualifiedName().toString(),
        SettingLocation.INTERFACE
    );
  }

  private Optional<RawSettings> findSettingsOn(PackageElement packageElement) {
    return this.findSettingsOn(
        packageElement,
        packageElement.getQualifiedName().toString(),
        SettingLocation.PACKAGE
    );
  }

  private Optional<RawSettings> findSettingsOn(
      AnnotatedConstruct annotated,
      String qualifiedName,
      SettingLocation location
  ) {
    return Optional
        .ofNullable(annotated.getAnnotation(Settings.class))
        .map(settings -> new RawSettings(qualifiedName, location, settings));
  }
}
