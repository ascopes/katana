package io.ascopes.katana.ap.descriptors;

import io.ascopes.katana.ap.settings.gen.SettingsCollection;
import io.ascopes.katana.ap.utils.Result;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Factory for inspecting and generating attributes to apply to models.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class AttributeFactory {

  /**
   * Parse attribute specific metadata and return the results in a stream.
   *
   * @param classifiedMethods the classified methods to consider.
   * @param settings          the settings to consider.
   * @return the map of successful attributes, mapping each attribute name to their descriptor.
   */
  public Result<SortedMap<String, Attribute>> buildFor(
      ClassifiedMethods classifiedMethods,
      SettingsCollection settings
  ) {
    // TODO: implement.
    return Result.ok(new TreeMap<>(String::compareTo));
  }
}
