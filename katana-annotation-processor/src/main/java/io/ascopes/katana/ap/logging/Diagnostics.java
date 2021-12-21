/*
 * Copyright (C) 2021 Ashley Scopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
   * @deprecated use {@link #builder(Class)} instead.
   * @return the builder.
   */
  @Deprecated
  @MustCall("kind")
  public KindStage builder() {
    // Frame 0: .getStackTrace()
    // Frame 1: .builder()
    // Frame 2: Callee frame.
    String className = Thread
        .currentThread()
        .getStackTrace()[2]
        .getClassName();
    return this.builder(className);
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
   * @param calleeClass the class that the message will be reported from. This affects the
   *     path that the handlebars template will be read from.
   * @return the builder.
   */
  @SuppressWarnings("deprecation")
  @MustCall("kind")
  public KindStage builder(Class<?> calleeClass) {
    return this.builder(calleeClass.getCanonicalName());
  }

  @Deprecated
  @MustCall("kind")
  private KindStage builder(String className) {
    String handlebarsTemplatePath = "/" + className.replace(".", "/");
    return new MessageBuilder(handlebarsTemplatePath);
  }

  private final class MessageBuilder
      implements KindStage, ElementStage, MessageStage, ParamsStage, FinishStage {

    private final String directory;
    private final Map<String, @Nullable Object> params;
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
    public ParamsStage param(String name, @Nullable Object value) {
      Objects.requireNonNull(name, "name of param was null");
      this.params.put(name, value);
      return this;
    }

    @Override
    @MustCall({"log", "render"})
    public ParamsStage param(String name, Throwable ex) {
      Objects.requireNonNull(name, "name of param was null");
      Objects.requireNonNull(ex, "Throwable was null");

      this.params.put(name, exToString(ex));

      return this;
    }

    @Override
    public void log() {
      try {
        String path = this.directory + "/" + this.template;
        String message = Diagnostics.this.handlebars
            .compile(path)
            .apply(this.params);

        Diagnostics.this.logger
            .debug("Reporting error to compiler [{}]:\n{}", path, message);

        Diagnostics.this.messager.printMessage(
            this.kind,
            message,
            this.element,
            this.annotationMirror,
            this.annotationValue
        );

      } catch (IOException | HandlebarsException ex) {
        String stackTrace = exToString(ex);

        Diagnostics.this.logger
            .error("Failed to generate error template:\n{}", stackTrace);

        throw new RuntimeException(ex);
      }
    }
  }

  private static String exToString(Throwable ex) {
    Objects.requireNonNull(ex, "Throwable was null");
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);

    ex.printStackTrace(printWriter);

    return ex.toString();
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
    ParamsStage param(String name, @Nullable Object value);


    /**
     * Add a parameter as a stacktrace of a given exception.
     *
     * @param name the name of the parameter in the handlebars template.
     * @param ex   the exception that should be rendered.
     * @return this stage.
     */
    @MustCall("log")
    default ParamsStage param(String name, Throwable ex);
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

