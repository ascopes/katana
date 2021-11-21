package io.ascopes.katana.ap.utils;

import com.github.jknack.handlebars.EscapingStrategy;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import com.github.jknack.handlebars.helper.DefaultHelperRegistry;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * Templating for diagnostic error messages ('compilation' errors). This keeps the code
 * tidier by delegating any rendering to the Handlebars templating engine. Error messages
 * are then defined as Handlebars templates in the classpath and are only loaded when needed.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class DiagnosticTemplates {

  private final Handlebars handlebars;

  public DiagnosticTemplates() {
    this.handlebars = new Handlebars()
        .with(new ClassPathTemplateLoader("/"))
        .with(new DefaultHelperRegistry()
            .registerHelpers(HandlebarsHelpers.class)
            .registerHelpers(StringHelpers.class)
            .registerHelpers(ConditionalHelpers.class))
        .with(EscapingStrategy.NOOP);
  }

  /**
   * <strong>This is only exposed for testing purposes.</strong>
   *
   * @return the handlebars internal instance.
   */
  Handlebars getHandlebars() {
    return this.handlebars;
  }

  /**
   * Render a template for the class that called this method.
   *
   * @param templateName the template name to render
   * @return the template builder to use.
   */
  public MessageBuilder template(String templateName) {
    return this.template(
        Thread.currentThread().getStackTrace()[2].getClassName(),
        templateName
    );
  }

  /**
   * Generate a new template builder for a template.
   *
   * @param className    the class that is generating the message.
   * @param templateName the name of the message template.
   * @return the builder.
   */
  private MessageBuilder template(String className, String templateName) {
    try {
      String path = className.replace('.', '/')
          + "Messages/"
          + templateName;
      Template template = this.handlebars.compile(path);
      return new MessageBuilder(template);
    } catch (HandlebarsException | IOException ex) {
      throw new RuntimeException("Failed to compile handlebars template", ex);
    }
  }

  /**
   * Builder helper for templates.
   */
  public static class MessageBuilder extends ObjectBuilder<String> {

    private final Template template;
    private final Map<String, @Nullable Object> args;

    private MessageBuilder(Template template) {
      Objects.requireNonNull(template);
      this.template = template;
      this.args = new HashMap<>();
    }

    public MessageBuilder placeholder(String name, @Nullable Object value) {
      Objects.requireNonNull(name);
      this.args.put(name, value);
      return this;
    }

    @Override
    public String build() {
      try {
        return this.template.apply(this.args);
      } catch (HandlebarsException | IOException ex) {
        throw new RuntimeException("Failed to parse template", ex);
      }
    }
  }
}
