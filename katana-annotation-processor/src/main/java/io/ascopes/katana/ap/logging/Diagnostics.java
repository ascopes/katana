package io.ascopes.katana.ap.logging;

import com.github.jknack.handlebars.EscapingStrategy;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import com.github.jknack.handlebars.helper.DefaultHelperRegistry;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import io.ascopes.katana.ap.utils.StringUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import org.checkerframework.checker.mustcall.qual.MustCall;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * Templating for diagnostic error messages ('compilation' errors). This keeps the code tidier by
 * delegating any rendering to the Handlebars templating engine. Error messages are then defined as
 * Handlebars templates in the classpath and are only loaded when needed.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class Diagnostics {

  private final Logger logger;
  private final Messager messager;
  private final Handlebars handlebars;

  public Diagnostics(Messager messager) {
    this.logger = LoggerFactory.loggerFor(this.getClass());
    this.messager = messager;
    this.handlebars = new Handlebars()
        .with(new ClassPathTemplateLoader("/"))
        .with(new DefaultHelperRegistry()
            .registerHelpers(StringUtils.class)
            .registerHelpers(StringHelpers.class)
            .registerHelpers(ConditionalHelpers.class))
        .with(EscapingStrategy.NOOP);
  }

  @MustCall("kind")
  public KindStage builder() {
    return this.builder(Thread.currentThread().getStackTrace()[2].getClassName());
  }

  @MustCall("kind")
  private KindStage builder(String className) {
    return new MessageBuilder("/" + className.replace(".", "/"));
  }

  public interface KindStage {

    @MustCall("template")
    ElementStage kind(Kind kind);
  }

  public interface ElementStage extends MessageStage {

    @MustCall("template")
    ElementStage element(@Nullable Element element);

    @MustCall("template")
    ElementStage annotationMirror(@Nullable AnnotationMirror annotationMirror);

    @MustCall("template")
    ElementStage annotationValue(@Nullable AnnotationValue annotationValue);

  }

  public interface MessageStage {

    @MustCall({"log", "param"})
    ParamsStage template(String template);
  }

  public interface ParamsStage extends FinishStage {

    @MustCall("log")
    ParamsStage param(String name, Object value);

    @MustCall("log")
    default ParamsStage param(String name, Throwable ex) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      ex.printStackTrace(printWriter);
      return this.param(name, stringWriter.toString());
    }
  }

  public interface FinishStage {

    void log();
  }

  private final class MessageBuilder
      implements KindStage, ElementStage, MessageStage, ParamsStage, FinishStage {

    private final String directory;
    private @MonotonicNonNull Kind kind;
    private @Nullable Element element;
    private @Nullable AnnotationMirror annotationMirror;
    private @Nullable AnnotationValue annotationValue;
    private @MonotonicNonNull String template;
    private final Map<String, Object> params;

    private MessageBuilder(String directory) {
      this.directory = Objects.requireNonNull(directory);
      this.params = new HashMap<>();
    }

    @Override
    @MustCall("template")
    public ElementStage kind(Kind kind) {
      this.kind = Objects.requireNonNull(kind);
      return this;
    }

    @Override
    @MustCall("template")
    public ElementStage element(@Nullable Element element) {
      this.element = element;
      return this;
    }

    @Override
    @MustCall("template")
    public ElementStage annotationMirror(@Nullable AnnotationMirror annotationMirror) {
      this.annotationMirror = annotationMirror;
      return this;
    }

    @Override
    @MustCall("template")
    public ElementStage annotationValue(@Nullable AnnotationValue annotationValue) {
      this.annotationValue = annotationValue;
      return this;
    }

    @Override
    @MustCall({"log", "render", "param"})
    public ParamsStage template(String template) {
      this.template = Objects.requireNonNull(template);
      return this;
    }

    @Override
    @MustCall({"log", "render"})
    public ParamsStage param(String name, Object value) {
      Objects.requireNonNull(name, "name of param was null");
      this.params.put(name, value);
      return this;
    }

    @Override
    public void log() {
      try {
        String path = this.directory + "/" + this.template;
        String message = Diagnostics.this.handlebars.compile(path).apply(this.params);
        Diagnostics.this.messager.printMessage(
            this.kind,
            message,
            this.element,
            this.annotationMirror,
            this.annotationValue
        );
      } catch (IOException | HandlebarsException ex) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Diagnostics.this.logger.error("Failed to generate template for diagnostics", ex);
      }
    }
  }
}

