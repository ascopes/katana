package io.ascopes.katana.ap.analysis;

import com.squareup.javapoet.TypeName;
import io.ascopes.katana.annotations.FieldVisibility;
import io.ascopes.katana.annotations.Visibility;
import io.ascopes.katana.ap.analysis.Attribute.AttributeBuilder;
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
        .entrySet()
        .stream()
        .map(pair -> this.createSingle(new AttributeCandidate(
            pair.getKey(),
            Attribute.builder(),
            pair.getValue(),
            settings,
            mutable
        )));
  }

  private Result<Attribute> createSingle(AttributeCandidate candidate) {

    ExecutableElement getter = candidate.getGetter();
    TypeName typeName = TypeName.get(getter.getReturnType());

    // Ensure we have a valid identifier.
    String name = candidate.getName();
    String identifierName = NamingUtils.transmogrifyIdentifier(name);

    candidate
        .getBuilder()
        .name(name)
        .identifier(identifierName)
        .type(typeName)
        .getter(getter);

    return Result
        .ok(candidate)
        .ifOkCheck(this::processFinal)
        .ifOkCheck(this::processTransience)
        .ifOkCheck(this::processFieldVisibility)
        .ifOkCheck(this::processSetter)
        .ifOkCheck(this::processEqualsAndHashCode)
        .ifOkCheck(this::processToStringInclusion)
        .ifOkCheck(this::processAttributeLevelDeprecation)
        .ifOkMap(AttributeCandidate::getBuilder)
        .ifOkMap(AttributeBuilder::build)
        .ifOk(attr -> this.logger.debug("Attribute creation for {} had result {}", name, attr));
  }

  private Result<Void> processFinal(AttributeCandidate candidate) {
    candidate.getBuilder().finalField(candidate.isMutableModel());
    return Result.ok();
  }

  private Result<Void> processTransience(AttributeCandidate candidate) {
    return this.featureManager
        .checkInclusion(
            candidate.getName(),
            candidate.getSettings().getFieldTransience(),
            candidate.getGetter()
        )
        .ifOk(candidate.getBuilder()::transientField)
        .ifOkDiscard();
  }

  private Result<Void> processFieldVisibility(AttributeCandidate candidate) {
    FieldVisibility fieldVisibility = candidate.getGetter().getAnnotation(FieldVisibility.class);
    Visibility visibility;

    if (fieldVisibility == null) {
      visibility = candidate.getSettings().getFieldVisibility().getValue();
    } else {
      visibility = fieldVisibility.value();
    }

    candidate.getBuilder().fieldVisibility(visibility);

    return Result.ok();
  }

  private Result<Void> processSetter(AttributeCandidate candidate) {
    return this.featureManager
        .checkInclusion(
            candidate.getName(),
            candidate.getSettings().getSetters(),
            candidate.getGetter()
        )
        .ifOk(candidate.getBuilder()::setterEnabled)
        // TODO(ascopes): allow overriding explicitly defined setters in the future
        .ifOkDiscard();
  }

  private Result<Void> processEqualsAndHashCode(AttributeCandidate candidate) {
    // This doesn't make any difference if we have a custom equality pair provided.
    return this.featureManager
        .checkInclusion(
            candidate.getName(),
            candidate.getSettings().getEqualityMode(),
            candidate.getGetter()
        )
        .ifOk(candidate.getBuilder()::includeInEqualsAndHashCode)
        .ifOkDiscard();
  }

  private Result<Void> processToStringInclusion(AttributeCandidate candidate) {
    // This doesn't make any difference if we have a custom toString provided.
    return this.featureManager
        .checkInclusion(
            candidate.getName(),
            candidate.getSettings().getToStringMode(),
            candidate.getGetter()
        )
        .ifOk(candidate.getBuilder()::includeInToString)
        .ifOkDiscard();
  }

  private Result<Void> processAttributeLevelDeprecation(AttributeCandidate candidate) {
    TypeElement deprecatedAnnotation = this.elementUtils
        .getTypeElement(Deprecated.class.getCanonicalName());

    AnnotationUtils
        .findAnnotationMirror(candidate.getGetter(), deprecatedAnnotation)
        .ifPresent(candidate.getBuilder()::deprecatedAnnotation);

    return Result.ok();
  }

  private static final class AttributeCandidate {

    private final String name;
    private final AttributeBuilder builder;
    private final ExecutableElement getter;
    private final SettingsCollection settings;
    private final boolean mutableModel;

    private AttributeCandidate(
        String name,
        AttributeBuilder builder,
        ExecutableElement getter,
        SettingsCollection settings,
        boolean mutableModel
    ) {
      this.name = Objects.requireNonNull(name);
      this.builder = Objects.requireNonNull(builder);
      this.getter = Objects.requireNonNull(getter);
      this.settings = Objects.requireNonNull(settings);
      this.mutableModel = mutableModel;
    }

    public String getName() {
      return this.name;
    }

    public ExecutableElement getGetter() {
      return this.getter;
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
