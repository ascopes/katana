package io.ascopes.katana.ap.codegen.init;

import io.ascopes.katana.ap.descriptors.Attribute;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Factory for initialization trackers.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class InitTrackerFactory {

  private final Logger logger;

  /**
   * Initialize this factory.
   */
  public InitTrackerFactory() {
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  /**
   * Create a new tracker for the given stream of required attributes.
   *
   * @param attributes        the stream of attributes to track.
   * @param trackingFieldName the tracking field name.
   * @return a tracker of the most efficient size to use for the given attributes.
   */
  public InitTracker createTracker(Stream<Attribute> attributes, String trackingFieldName) {
    SortedSet<Attribute> attributeSet = attributes
        .collect(Collectors.toCollection(() -> new TreeSet<>(
            Comparator.comparing(Attribute::toString))
        ));

    if (attributeSet.size() < Integer.SIZE) {
      this.logger.debug("Using an int tracker for tracking initialized attributes");
      return new IntInitTrackerImpl(attributeSet, trackingFieldName);
    }

    if (attributeSet.size() < Long.SIZE) {
      this.logger.debug("Using a long tracker for tracking initialized attributes");
      return new LongInitTrackerImpl(attributeSet, trackingFieldName);
    }

    this.logger.debug("Using a BigInteger tracker for tracking initialized attributes");
    return new BigIntegerInitTrackerImpl(attributeSet, trackingFieldName);
  }
}
