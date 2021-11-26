package io.ascopes.katana.ap.codegen.init;

import io.ascopes.katana.ap.descriptors.Attribute;
import io.ascopes.katana.ap.utils.Logger;
import java.util.SortedSet;

/**
 * Factory for initialization trackers.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class InitTrackerFactory {

  private final Logger logger;

  public InitTrackerFactory() {
    this.logger = new Logger();
  }

  /**
   * @param attributes the attributes to track. This must be a sorted set to ensure deterministic
   *                   behaviour between builds (which makes debugging and testing far simpler).
   * @return a tracker of the most efficient size to use for the given attributes.
   */
  public InitTracker createTracker(SortedSet<Attribute> attributes) {
    if (attributes.size() < Integer.SIZE) {
      this.logger.debug("Using an int tracker for tracking initialized attributes");
      return new IntInitTracker(attributes);
    }

    if (attributes.size() < Long.SIZE) {
      this.logger.debug("Using a long tracker for tracking initialized attributes");
      return new LongInitTracker(attributes);
    }

    this.logger.debug("Using a BigInteger tracker for tracking initialized attributes");
    return new BigIntegerInitTracker(attributes);
  }
}
