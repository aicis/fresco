package dk.alexandra.fresco.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPerformancePrinter implements PerformancePrinter {

  public Logger log = LoggerFactory.getLogger(PerformanceLogger.class);
  
  @Override
  public void printPerformanceLog(PerformanceLogger logger, int myId) {
    String output = logger.makeLogString(myId);
    log.info(output);
  }

}
