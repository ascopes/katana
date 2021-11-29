package io.ascopes.katana.ap.logging;

import com.github.jknack.handlebars.EscapingStrategy;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import com.github.jknack.handlebars.helper.DefaultHelperRegistry;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import io.ascopes.katana.ap.utils.HandlebarsHelpers;
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

  /**
   * Initialize this diagnostics helper.
   *
   * @param messager the diagnostics to initialize.
   */
  public Diagnostics(Messager messager) {
    this.logger = LoggerFactory.loggerFor(this.getClass());
    this.messager = messager;
    this.handlebars = new Handlebars()
        .with(new ClassPathTemplateLoader("/"))
        .with(new DefaultHelperRegistry()
            .registerHelpers(HandlebarsHelpers.class)
            .registerHelpers(StringHelpers.class)
            .registerHelpers(ConditionalHelpers.class))
        .with(EscapingStrategy.NOOP);
  }

  /**
   * Return a builder for a diagnostic message that will be sent to the compiler once completed.
   *
   * <p>If the kind is {@link Kind#ERROR}, the compilation will be aborted at some point before
   * the process is killed.
   *
   * <p>Likewise, if the kind is {@link Kind#WARNING} or {@link Kind#MANDATORY_WARNING}, and
   * {@code -Werror} is enabled on the compiler, then this will also result in the compilation
   * aborting at some point before the process is killed.
   *
   * @return the builder.
   */
  @MustCall("kind")
  public KindStage builder() {
    return this.builder(Thread.currentThread().getStackTrace()[2].getClassName());
  }

  @MustCall("kind")
  private KindStage builder(String className) {
    return new MessageBuilder("/" + className.replace(".", "/"));
  }

  private final class MessageBuilder
      implements KindStage, ElementStage, MessageStage, ParamsStage, FinishStage {

    private final String directory;
    private final Map<String, Object> params;
    private @MonotonicNonNull Kind kind;
    private @Nullable Element element;
    private @Nullable AnnotationMirror annotationMirror;
    private @Nullable AnnotationValue annotationValue;
    private @MonotonicNonNull String template;

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

  /**
   * Stage for the diagnostics builder which allows the specification of the diagnostic message kind
   * to use.
   */
  public interface KindStage {

    /**
     * Set the kind of the message.
     *
     * @param kind the kind to use.
     * @return the element-specification stage.
     */
    @MustCall("template")
    ElementStage kind(Kind kind);
  }

  /**
   * Stage for the diagnostics builder which allows the specification of an optional element,
   * annotation mirror, and annotation value to show in compiler diagnostics.
   *
   * <p>This can be ignored and the message stage jumped to immediately (this also provides
   * access to that stage).
   */
  public interface ElementStage extends MessageStage {

    /**
     * Set the element to show in diagnostics.
     *
     * @param element the element to show in diagnostics.
     * @return this stage.
     */
    @MustCall("template")
    ElementStage element(@Nullable Element element);

    /**
     * Set the annotation mirror to show in diagnostics.
     *
     * <p>If you call this, you also need to call {@link #element(Element)} otherwise the results
     * are undefined.
     *
     * @param annotationMirror the annotation mirror to show in diagnostics.
     * @return this stage.
     */
    @MustCall({"template", "element"})
    ElementStage annotationMirror(@Nullable AnnotationMirror annotationMirror);

    /**
     * Set the annotation value to show in diagnostics.
     *
     * <p>If you call this, you also need to call {@link #annotationMirror(AnnotationMirror)}
     * otherwise the results are undefined.
     *
     * @param annotationValue the annotation value to show in diagnostics.
     * @return this stage.
     */
    @MustCall({"template", "element", "annotationMirror"})
    ElementStage annotationValue(@Nullable AnnotationValue annotationValue);

  }

  /**
   * Stage for the diagnostics builder which allows the specification of the template name to use.
   */
  public interface MessageStage {

    /**
     * Specify the handlebars template to render.
     *
     * @param template the template name to use. This should not include a file extension in the
     *                 name (the actual file must have the {@code *.hbs} extension), and must be
     *                 located in a classpath directory matching the package name, within a
     *                 directory matching the name of the class this is being called from.
     * @return the parameter specification stage.
     */
    @MustCall({"log", "param"})
    ParamsStage template(String template);
  }

  /**
   * Stage for the diagnostics builder which allows the specification of the parameters to render.
   */
  public interface ParamsStage extends FinishStage {

    /**
     * Add a parameter.
     *
     * @param name  the name of the parameter in the handlebars template.
     * @param value the value of the parameter.
     * @return this stage.
     */
    @MustCall("log")
    ParamsStage param(String name, Object value);


    /**
     * Add a parameter as a stacktrace of a given exception.
     *
     * @param name the name of the parameter in the handlebars template.
     * @param ex   the exception that should be rendered.
     * @return this stage.
     */
    @MustCall("log")
    default ParamsStage param(String name, Throwable ex) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      ex.printStackTrace(printWriter);
      return this.param(name, stringWriter.toString());
    }
  }

  /**
   * Final stage in message rendering.
   */
  public interface FinishStage {

    /**
     * Commit the rendered message to the compiler diagnostics.
     */
    void log();
  }

}

