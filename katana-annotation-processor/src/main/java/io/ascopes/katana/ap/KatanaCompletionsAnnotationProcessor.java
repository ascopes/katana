package io.ascopes.katana.ap;

import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.Completion;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * Entrypoint for the Katana completions processor that provides IDE support for autocomplete.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class KatanaCompletionsAnnotationProcessor extends AbstractKatanaAnnotationProcessor {

  @Override
  protected void doInit() {
    // Do nothing.
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    // Do nothing.
    return true;
  }

  @Override
  public Iterable<? extends Completion> getCompletions(
      Element element,
      AnnotationMirror annotation,
      ExecutableElement member,
      String userText
  ) {
    // TODO: implement.
    this.logger.info("Fetching completions for text '{}'", userText);
    return Collections.emptyList();
  }
}
