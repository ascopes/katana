package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.annotations.Setters;
import io.ascopes.katana.ap.descriptors.Attribute.Builder;
import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import io.ascopes.katana.ap.utils.AnnotationUtils;
import io.ascopes.katana.ap.utils.DiagnosticTemplates;
import io.ascopes.katana.ap.utils.Functors;
import io.ascopes.katana.ap.utils.Result;
import io.ascopes.katana.ap.utils.ResultCollector;
import java.util.SortedMap;
import java.util.function.Function;
import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;

/**
 * Factory for inspecting and generating attributes to apply to models.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class AttributeFactory {

  private final Elements elementUtils;
  private final Messager messager;
  private final DiagnosticTemplates diagnosticTemplates;

  public AttributeFactory(
      DiagnosticTemplates diagnosticTemplates,
      Messager messager,
      Elements elementUtils
  ) {
    this.diagnosticTemplates = diagnosticTemplates;
    this.messager = messager;
    this.elementUtils = elementUtils;
  }

  /**
   * Parse attribute specific metadata and return the results in a stream.
   *
   * @param classifiedMethods the classified methods to consider.
   * @param settings          the settings to consider.
   * @return the map of successful attributes, mapping each attribute name to their descriptor.
   */
  public Result<SortedMap<String, Attribute>> buildFor(
      ClassifiedMethods classifiedMethods,
      SettingsCollection settings
  ) {
    Result<SortedMap<String, Attribute>> attributes = classifiedMethods
        .getGetters()
        .keySet()
        .stream()
        .map(attr -> this.buildFor(attr, classifiedMethods, settings))
        .collect(ResultCollector.aggregating(
            Functors.toSortedMap(Attribute::getName, Function.identity(), String::compareTo)
        ));

    // Give up if we have orphan methods. Do this after processing attributes though so that
    // we can provide a meaningful set of error messages for those as well.
    return this
        .ensureNoOrphans(classifiedMethods)
        .ifOkFlatReplace(attributes);
  }

  private Result<Attribute> buildFor(
      String attributeName,
      ClassifiedMethods classifiedMethods,
      SettingsCollection settings
  ) {
    ExecutableElement getter = classifiedMethods.getGetters().get(attributeName);

    Attribute.Builder builder = Attribute
        .builder()
        .name(attributeName)
        .getterToOverride(getter);

    return this
        .processSetter(builder, classifiedMethods, settings)
        .ifOkMap(Builder::build);
  }

  private Result<Attribute.Builder> processSetter(
      Attribute.Builder builder,
      ClassifiedMethods classifiedMethods,
      SettingsCollection settings
  ) {
    Setters setterMode = settings.getSetters().getValue();

    if (setterMode == Setters.DISABLED) {
      // Do not bother to do anything, it is not enabled anywhere.
      return Result.ok(builder.setterEnabled(false));
    }

    ExecutableElement getter = builder.getGetterToOverride();
    ExecutableElement explicitSetter = classifiedMethods.getSetters().get(builder.getName());

    TypeElement includeAnnotation = this.elementUtils
        .getTypeElement(Setters.Include.class.getCanonicalName());
    TypeElement excludeAnnotation = this.elementUtils
        .getTypeElement(Setters.Exclude.class.getCanonicalName());

    Result<? extends AnnotationMirror> include = AnnotationUtils
        .findAnnotationMirror(getter, includeAnnotation);
    Result<? extends AnnotationMirror> exclude = AnnotationUtils
        .findAnnotationMirror(getter, excludeAnnotation);

    if (explicitSetter != null && exclude.isOk()) {
      this.failExcludedExplicitSetter(builder.getName(), explicitSetter, exclude.unwrap());
      return Result.fail();
    }

    if (include.isOk() && exclude.isOk()) {
      this.failIncludedAndExcluded(
          "setter",
          builder.getName(),
          getter,
          include.unwrap(),
          exclude.unwrap()
      );
      return Result.fail();
    }

    if (setterMode == Setters.INCLUDE_ALL && exclude.isOk()) {
      // Manually opted-out.
      builder.setterEnabled(false);
    } else if (setterMode == Setters.EXCLUDE_ALL && include.isOk()) {
      // Manually opted-in.
      builder.setterEnabled(true);
    } else {
      // Enable if include all is defaulted.
      builder.setterEnabled(setterMode == Setters.INCLUDE_ALL);
    }

    if (explicitSetter != null) {
      // Explicit setter provided.
      return Result.ok(builder.setterToOverride(explicitSetter));
    }

    return Result.ok(builder);
  }

  private Result<ClassifiedMethods> ensureNoOrphans(ClassifiedMethods classifiedMethods) {
    for (String attributeName : classifiedMethods.getSetters().keySet()) {
      if (!classifiedMethods.getGetters().containsKey(attributeName)) {
        this.failMissingGetter(
            "setter",
            attributeName,
            classifiedMethods.getSetters().get(attributeName)
        );
        return Result.fail();
      }
    }

    return Result.ok(classifiedMethods);
  }

  private void failMissingGetter(
      @SuppressWarnings("SameParameterValue") String category,
      String attributeName,
      ExecutableElement definedMethod
  ) {
    String message = this.diagnosticTemplates
        .template(this.getClass(), "missingGetter")
        .placeholder("category", category)
        .placeholder("attributeName", attributeName)
        .placeholder("definedMethod", definedMethod.toString())
        .build();

    this.messager.printMessage(
        Kind.ERROR,
        message,
        definedMethod
    );
  }

  private void failExcludedExplicitSetter(
      String attributeName,
      ExecutableElement explicitSetter,
      AnnotationMirror exclusionAnnotation
  ) {
    String message = this.diagnosticTemplates
        .template(this.getClass(), "excludedExplicitSetter")
        .placeholder("attributeName", attributeName)
        .placeholder("explicitSetter", explicitSetter)
        .placeholder("exclusionAnnotation", exclusionAnnotation)
        .build();

    this.messager.printMessage(
        Kind.ERROR,
        message,
        explicitSetter,
        exclusionAnnotation
    );
  }

  private void failIncludedAndExcluded(
      @SuppressWarnings("SameParameterValue") String category,
      String attributeName,
      ExecutableElement getter,
      AnnotationMirror inclusionAnnotation,
      AnnotationMirror exclusionAnnotation
  ) {
    String message = this.diagnosticTemplates
        .template(this.getClass(), "includedAndExcluded")
        .placeholder("attributeName", attributeName)
        .placeholder("category", category)
        .placeholder("getter", getter)
        .placeholder("includeAnnotation", inclusionAnnotation)
        .placeholder("excludeAnnotation", exclusionAnnotation)
        .build();

    this.messager.printMessage(
        Kind.ERROR,
        message,
        getter,
        inclusionAnnotation
    );
  }

}
