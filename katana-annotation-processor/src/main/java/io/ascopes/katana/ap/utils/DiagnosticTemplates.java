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

public class DiagnosticTemplates {

  private final Handlebars handlebars;

  public DiagnosticTemplates() {
    this.handlebars = new Handlebars()
        .with(new ClassPathTemplateLoader("/io/ascopes/katana/ap/messages/", ".txt"))
        .with(new DefaultHelperRegistry()
            .registerHelpers(HandlebarsHelpers.class)
            .registerHelpers(StringHelpers.class)
            .registerHelpers(ConditionalHelpers.class))
        .with(EscapingStrategy.NOOP);
  }

  /**
   * Generate a new template builder for a template.
   *
   * @param klass        the class that is generating the message.
   * @param templateName the name of the message template.
   * @return the builder.
   */
  public MessageBuilder template(Class<?> klass, String templateName) {
    try {
      Template template = this.handlebars.compile(klass.getSimpleName() + "/" + templateName);
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
    private final Map<String, Object> args;

    private MessageBuilder(Template template) {
      this.template = template;
      this.args = new HashMap<>();
    }

    public MessageBuilder placeholder(String name, Object value) {
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
