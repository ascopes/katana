package io.ascopes.katana.ap.descriptors;

import com.squareup.javapoet.TypeName;
import io.ascopes.katana.annotations.FieldVisibility;
import io.ascopes.katana.annotations.Visibility;
import io.ascopes.katana.ap.descriptors.Attribute.AttributeBuilder;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import io.ascopes.katana.ap.utils.AnnotationUtils;
import io.ascopes.katana.ap.utils.NamingUtils;
import io.ascopes.katana.ap.utils.Result;
import java.util.Objects;
import java.util.stream.Stream;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Factory for inspecting and generating attributes to apply to models.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
final class AttributeFactory {

  private final Logger logger;
  private final Elements elementUtils;
  private final FeatureManager featureManager;

  /**
   * Initialize this factory.
   *
   * @param featureManager the feature manager to use.
   * @param elementUtils   the element utilities to use for introspection.
   */
  AttributeFactory(
      FeatureManager featureManager,
      Elements elementUtils
  ) {
    this.logger = LoggerFactory.loggerFor(AttributeFactory.class);
    this.elementUtils = elementUtils;
    this.featureManager = featureManager;
  }

  /**
   * Create a stream of attributes for a given method classification and settings.
   *
   * @param methodClassification the classified methods on the model interface to consider.
   * @param settings             the settings to use.
   * @param mutable              true if the model is mutable, false otherwise.
   * @return the stream of attributes. Each wrapped in an OK result if successfully created or an
   *     empty failed result if something went wrong.
   */
  Stream<Result<Attribute>> create(
      MethodClassification methodClassification,
      SettingsCollection settings,
      boolean mutable
  ) {
    return methodClassification
        .getGetters()
        .keySet()
        .stream()
        .map(attr -> this.buildFor(attr, methodClassification, mutable, settings));
  }

  private Result<Attribute> buildFor(
      String attributeName,
      MethodClassification methodClassification,
      boolean mutable,
      SettingsCollection settings
  ) {
    // Expect this to always be present.
    ExecutableElement getter = Objects
        .requireNonNull(methodClassification.getGetters().get(attributeName));
    TypeName typeName = TypeName.get(getter.getReturnType());

    // Ensure we have a valid identifier.
    String identifierName = NamingUtils.transmogrifyIdentifier(attributeName);

    Result<Attribute> result = Result
        .ok(Attribute
            .builder()
            .name(attributeName)
            .identifier(identifierName)
            .type(typeName)
            .getter(getter))
        .ifOkFlatMap(builder -> this.processFinal(builder, mutable, settings))
        .ifOkFlatMap(builder -> this.processTransience(builder, settings))
        .ifOkFlatMap(builder -> this.processFieldVisibility(builder, settings))
        .ifOkFlatMap(builder -> this.processSetter(builder, settings))
        .ifOkFlatMap(builder -> this.processToString(builder, settings))
        .ifOkFlatMap(builder -> this.processEqualsAndHashCode(builder, settings))
        .ifOk(this::processAttributeLevelDeprecation)
        .ifOkMap(AttributeBuilder::build);

    this.logger.debug("Attribute creation for {} had result {}", attributeName, result);

    return result;
  }

  private Result<AttributeBuilder> processFinal(
      AttributeBuilder builder,
      boolean mutable,
      @SuppressWarnings("unused") SettingsCollection settings
  ) {
    return Result
        .ok(builder.finalField(!mutable));
  }

  private Result<AttributeBuilder> processTransience(
      AttributeBuilder builder,
      SettingsCollection settings
  ) {
    return this.featureManager
        .checkInclusion(builder.getName(), settings.getFieldTransience(), builder.getGetter())
        .ifOkMap(builder::transientField);
  }

  private Result<AttributeBuilder> processFieldVisibility(
      AttributeBuilder builder,
      SettingsCollection settings
  ) {
    FieldVisibility fieldVisibility = builder.getGetter().getAnnotation(FieldVisibility.class);
    Visibility visibility;

    if (fieldVisibility == null) {
      visibility = settings.getFieldVisibility().getValue();
    } else {
      visibility = fieldVisibility.value();
    }

    return Result
        .ok(builder.fieldVisibility(visibility));
  }

  private Result<AttributeBuilder> processSetter(
      AttributeBuilder builder,
      SettingsCollection settings
  ) {
    return this.featureManager
        .checkInclusion(builder.getName(), settings.getSetters(), builder.getGetter())
        .ifOk(builder::setterEnabled)
        // TODO(ascopes): allow overriding explicitly defined setters in the future
        .ifOkReplace(() -> Result.ok(builder));
  }

  private Result<AttributeBuilder> processToString(
      AttributeBuilder builder,
      SettingsCollection settings
  ) {
    return this.featureManager
        .checkInclusion(builder.getName(), settings.getToStringMode(), builder.getGetter())
        .ifOkMap(builder::includeInToString);
  }

  private Result<AttributeBuilder> processEqualsAndHashCode(
      AttributeBuilder builder,
      SettingsCollection settings
  ) {
    return this.featureManager
        .checkInclusion(builder.getName(), settings.getEqualityMode(), builder.getGetter())
        .ifOkMap(builder::includeInEqualsAndHashCode);
  }

  private void processAttributeLevelDeprecation(AttributeBuilder builder) {
    TypeElement deprecatedAnnotation = this.elementUtils
        .getTypeElement(Deprecated.class.getCanonicalName());

    AnnotationUtils
        .findAnnotationMirror(builder.getGetter(), deprecatedAnnotation)
        .ifPresent(builder::deprecatedAnnotation);
  }

  private static final class AttributeCandidate {

    private final AttributeBuilder builder;
    private final SettingsCollection settings;
    private final boolean mutableModel;

    private AttributeCandidate(
        AttributeBuilder builder,
        SettingsCollection settings,
        boolean mutableModel
    ) {
      this.builder = builder;
      this.settings = settings;
      this.mutableModel = mutableModel;
    }

    public AttributeBuilder getBuilder() {
      return this.builder;
    }

    public SettingsCollection getSettings() {
      return this.settings;
    }

    public boolean isMutableModel() {
      return this.mutableModel;
    }
  }
}
