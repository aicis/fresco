package dk.alexandra.fresco.logging;

import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPerformancePrinter implements PerformancePrinter {

  public Logger log = LoggerFactory.getLogger(PerformanceLogger.class);
  
  @Override
  public void printPerformanceLog(PerformanceLogger logger, int myId) {
    String s = "Logger for "+this.getClass().getName()+": ";
    for (Entry<String, Long> e : logger.getLoggedValues(myId).entrySet()) {
      s += "["+e.getKey().toString() + ": " + e.getValue().toString()+"]";
    }
    log.info(s);
  }
}
