package dk.alexandra.fresco.logging;

import java.util.Map;

/**
 * Interface for loggers recording performance metrics.
 */
public interface PerformanceLogger {

  /**
   * Resets any logged values.
   */
  public void reset();

  /**
   * Produces a map from a named metric to the values logged for that metric.
   */
  public Map<String, Long> getLoggedValues();

}
