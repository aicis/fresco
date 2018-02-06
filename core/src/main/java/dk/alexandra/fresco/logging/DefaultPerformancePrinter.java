package dk.alexandra.fresco.logging;

import java.util.Map.Entry;
import org.slf4j.LoggerFactory;

public class DefaultPerformancePrinter implements PerformancePrinter {

  @Override
  public void printPerformanceLog(PerformanceLogger logger) {
    String s = "";
    for (Entry<String, Long> e : logger.getLoggedValues().entrySet()) {
      s += "[" + e.getKey().toString() + ": " + e.getValue().toString() + "]";
    }
    LoggerFactory.getLogger(logger.getClass()).info(s);
  }
}
