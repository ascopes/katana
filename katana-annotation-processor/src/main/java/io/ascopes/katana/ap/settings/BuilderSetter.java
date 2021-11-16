package io.ascopes.katana.ap.settings;

import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import java.util.function.BiConsumer;

/**
 * A consumer that sets the value of a setting on a SettingsCollection builder instance..
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@FunctionalInterface
public interface BuilderSetter<T> extends BiConsumer<SettingsCollection.Builder, Setting<T>> {
  // Type alias
}
