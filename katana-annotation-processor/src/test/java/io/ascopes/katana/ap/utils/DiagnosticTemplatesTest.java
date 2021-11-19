package io.ascopes.katana.ap.utils;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

class DiagnosticTemplatesTest {

  static DiagnosticTemplates templates;

  @BeforeAll
  static void setUpAll() {
    templates = new DiagnosticTemplates();
  }

  @Order(1)
  @Test
  void can_resolve_and_render_basic_template() {
    // given
    String message = templates
        .template("java")
        .placeholder("language", "Java")
        .placeholder("company", "Sun Microsystems")
        .placeholder("year", 1995)
        .build();

    // then
    BDDAssertions.then(message)
        .isEqualToIgnoringNewLines(
            "Java is a programming language and computing platform first released by\n"
                + "Sun Microsystems in 1995."
        );
  }

  @Order(2)
  @Test
  void handlebars_helpers_registered() {
    BDDAssertions.then(templates.getHandlebars().helpers()).isNotEmpty();
  }

  @Order(3)
  @Test
  void can_invoke_handlebars_helpers() {
    // given
    String message = templates
        .template("helpers")
        .placeholder("animal", "cat")
        .placeholder("sound", "meow")
        .build();

    // then
    BDDAssertions.then(message)
        .isEqualToIgnoringNewLines("I would like to be a cat.\n\nMEOW!!!");
  }
}
