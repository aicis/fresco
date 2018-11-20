package dk.alexandra.fresco.logging;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MatrixLogger implements PerformanceLogger {

  private Map<String, Instant> startTimes;
  private Map<String, Instant> endTimes;

  public MatrixLogger() {
    this.startTimes = new HashMap<>();
    this.endTimes = new HashMap<>();
  }

  /**
   * Starts timing of a named task.
   *
   * @param taskName the name of the task
   */
  public void startTask(String taskName) {
    startTimes.put(taskName, Instant.now());
  }

  /**
   * Ends the timing of a named task.
   *
   * @param taskName name of the task
   * @throws IllegalArgumentException if {@code taskName} was not started using
   *         {@link this#startTask(String)} prior to this call.
   */
  public void endTask(String taskName) {
    if (!startTimes.containsKey(taskName)) {
      throw new IllegalArgumentException("Cannot end \"" + taskName + "\" that was never started.");
    }
    endTimes.put(taskName, Instant.now());
  }

  @Override
  public void reset() {
    this.startTimes = new HashMap<>();
    this.endTimes = new HashMap<>();
  }

  @Override
  public Map<String, Long> getLoggedValues() {
    Map<String, Long> runningTimes = new HashMap<>();
    for (String key : startTimes.keySet()) {
      if (endTimes.containsKey(key)) {
        long time = Duration.between(startTimes.get(key), endTimes.get(key)).get(ChronoUnit.MILLIS);
        runningTimes.put(key, time);
      } else {
        throw new IllegalStateException("Task \"" + key + "\" not ended.");
      }
    }
    return runningTimes;
  }

}
