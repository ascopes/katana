package io.ascopes.katana.ap.codegen.components;

import com.squareup.javapoet.MethodSpec;
import io.ascopes.katana.ap.descriptors.Constructor;
import io.ascopes.katana.ap.descriptors.Model;
import io.ascopes.katana.ap.utils.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Factory for creating general purpose public constructors.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class ConstructorFactory {

  private final Logger logger;

  public ConstructorFactory() {
    this.logger = new Logger();
  }

  public Iterable<MethodSpec> create(Model model) {
    Set<Constructor> constructors = model.getConstructors();
    List<MethodSpec> methods = new ArrayList<>();

    // TODO(ascopes): implement.
    return Collections.emptyList();
  }
}
