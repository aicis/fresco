package dk.alexandra.fresco.logging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates a new aggregater of the performance counters.
 */
public class PerformanceLoggerCountingAggregate implements PerformanceLogger {


  private List<PerformanceLogger> performanceLoggers = new ArrayList<>();

  @Override
  public void reset() {
    for (PerformanceLogger performanceLogger : performanceLoggers) {
      performanceLogger.reset();
    }
  }

  @Override
  public Map<String, Long> getLoggedValues(int partyId) {
    Map<String, Long> result = new HashMap<>();
    for (PerformanceLogger performanceLogger : performanceLoggers) {
      Map<String, Long> loggedValues = performanceLogger.getLoggedValues(partyId);
      for (String key : loggedValues.keySet()) {
        result.merge(key, loggedValues.get(key), (left, right) -> left + right);
      }
    }
    return result;
  }

  /**
   * Adds a performance logger to this.
   *
   * @param performanceLogger the sub logger to add.
   */
  public void add(PerformanceLogger performanceLogger) {
    performanceLoggers.add(performanceLogger);
  }
}
