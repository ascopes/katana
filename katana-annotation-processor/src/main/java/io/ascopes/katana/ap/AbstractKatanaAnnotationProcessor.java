package io.ascopes.katana.ap;

import io.ascopes.katana.annotations.ImmutableModel;
import io.ascopes.katana.annotations.MutableModel;
import io.ascopes.katana.ap.utils.Logger;
import io.ascopes.katana.ap.utils.Logger.Level;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;

/**
 * Abstract base for an annotation processor.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public abstract class AbstractKatanaAnnotationProcessor extends AbstractProcessor {

  private static final Class<MutableModel> MUTABLE_ANNOTATION = MutableModel.class;
  private static final Class<ImmutableModel> IMMUTABLE_ANNOTATION = ImmutableModel.class;
  private static final String LOGGING_LEVEL = "logging.level";

  protected final Logger logger;

  public AbstractKatanaAnnotationProcessor() {
    this.logger = new Logger();
  }

  /**
   * @return supported options for annotation processors.
   */
  @Override
  public final Set<String> getSupportedOptions() {
    return Collections.singleton(LOGGING_LEVEL);
  }

  /**
   * @return the supported annotation types.
   */
  @Override
  public final Set<String> getSupportedAnnotationTypes() {
    return Stream.of(MUTABLE_ANNOTATION, IMMUTABLE_ANNOTATION)
        .map(Class::getCanonicalName)
        .collect(Collectors.toSet());
  }

  /**
   * @return the supported source version.
   */
  @Override
  public SourceVersion getSupportedSourceVersion() {
    // We support up to JDK-17 at the time of writing, but we do not have access to that constant,
    // so just bodge in the current compiler version and hope for the best.
    return SourceVersion.latestSupported();
  }

  @Override
  public synchronized final void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    // Init the loggers.
    Optional
        .ofNullable(processingEnv.getOptions().get(LOGGING_LEVEL))
        .map(Level::valueOf)
        .ifPresent(Logger::setGlobalLevel);

    this.doInit();
  }

  /**
   * Run any initialization steps.
   */
  protected abstract void doInit();
}
