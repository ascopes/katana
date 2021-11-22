package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.descriptors.Attribute.Builder;
import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import io.ascopes.katana.ap.utils.DiagnosticTemplates;
import io.ascopes.katana.ap.utils.Functors;
import io.ascopes.katana.ap.utils.Result;
import io.ascopes.katana.ap.utils.ResultCollector;
import java.util.SortedMap;
import java.util.function.Function;
import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic.Kind;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Factory for inspecting and generating attributes to apply to models.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class AttributeFactory {

  private final Messager messager;
  private final DiagnosticTemplates diagnosticTemplates;
  private final AttributeFeatureInclusionManager attributeFeatureInclusionManager;

  public AttributeFactory(
      DiagnosticTemplates diagnosticTemplates,
      AttributeFeatureInclusionManager attributeFeatureInclusionManager,
      Messager messager
  ) {
    this.diagnosticTemplates = diagnosticTemplates;
    this.attributeFeatureInclusionManager = attributeFeatureInclusionManager;
    this.messager = messager;
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
        .ifOkReplace(() -> attributes);
  }

  private Result<Attribute> buildFor(
      String attributeName,
      ClassifiedMethods classifiedMethods,
      SettingsCollection settings
  ) {
    return Result
        .ok(Attribute
            .builder()
            .name(attributeName)
            .getterToOverride(classifiedMethods.getGetters().get(attributeName)))
        .ifOkFlatMap(builder -> this.processSetter(builder, classifiedMethods, settings))
        .ifOkFlatMap(builder -> this.processToString(builder, settings))
        .ifOkFlatMap(builder -> this.processEqualsAndHashCode(builder, settings))
        .ifOkMap(Builder::build);
  }

  private Result<Attribute.Builder> processSetter(
      Attribute.Builder builder,
      ClassifiedMethods classifiedMethods,
      SettingsCollection settings
  ) {
    @Nullable
    ExecutableElement setter = classifiedMethods.getSetters().get(builder.getName());

    return this.attributeFeatureInclusionManager
        .check(builder.getName(), settings.getSetters(), builder.getGetterToOverride())
        .ifOkThen(builder::setterEnabled)
        .ifOkFlatMap(enabled -> {
          if (setter != null) {
            if (enabled) {
              builder.setterToOverride(setter);
            } else {
              this.failExcludedExplicitSetter(builder.getName(), setter);
              return Result.fail();
            }
          }
          return Result.ok(enabled);
        })
        .ifOkReplace(() -> Result.ok(builder));
  }

  private Result<Attribute.Builder> processToString(
      Attribute.Builder builder,
      SettingsCollection settings
  ) {
    return this.attributeFeatureInclusionManager
        .check(builder.getName(), settings.getToStringMethod(), builder.getGetterToOverride())
        .ifOkMap(builder::includeInToString);
  }

  private Result<Attribute.Builder> processEqualsAndHashCode(
      Attribute.Builder builder,
      SettingsCollection settings
  ) {
    return this.attributeFeatureInclusionManager
        .check(builder.getName(), settings.getEqualsAndHashCode(), builder.getGetterToOverride())
        .ifOkMap(builder::includeInEqualsAndHashCode);
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
        .template("missingGetter")
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
      ExecutableElement explicitSetter
  ) {
    String message = this.diagnosticTemplates
        .template("excludedExplicitSetter")
        .placeholder("attributeName", attributeName)
        .placeholder("explicitSetter", explicitSetter)
        .build();

    this.messager.printMessage(
        Kind.ERROR,
        message,
        explicitSetter
    );
  }
}
