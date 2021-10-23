package io.ascopes.katana.ap.settings;

import io.ascopes.katana.annotations.ImmutableModel;
import io.ascopes.katana.annotations.MutableModel;

/**
 * Valid locations for individual settings.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public enum SettingLocation {
  /**
   * The setting was set on an annotation level (i.e. in {@link ImmutableModel#settings()}
   * or {@link MutableModel#settings()}.
   */
  ANNOTATION,

  /**
   * The setting was set on a model that was annotated with one or more of
   * {@link ImmutableModel} or {@link MutableModel}.
   */
  INTERFACE,

  /**
   * The setting was set on a package level.
   */
  PACKAGE,
}
