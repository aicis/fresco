package dk.alexandra.fresco.logging;

import java.text.DecimalFormat;
import java.util.EnumSet;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for getting performance numbers. Use the EnumSet to indicate which parameters to log. Note
 * that network logging is done on ONLY the data received and used within FRESCO. This means that if
 * the network implementation uses double the bytes to wrap the messages, this will not show.
 *
 */
public interface PerformanceLogger {

  public Logger log = LoggerFactory.getLogger(PerformanceLogger.class);
  public DecimalFormat df = new DecimalFormat("#.00");
  
  public enum Flag {
    LOG_NETWORK, LOG_EVALUATOR, LOG_NATIVE_BATCH;

    public static final EnumSet<Flag> ALL_OPTS = EnumSet.allOf(Flag.class);
  }

  /**
   * Prints any performance numbers picked up using the
   * default logger.
   */
  public default void printPerformanceLog(int myId) {
    printToLog(PerformanceLogger.log, myId);
  }
  
  /**
   * Prints any performance numbers picked up using the
   * specified logger. 
   */
  public void printToLog(Logger log, int myId);
  
  /**
   * Resets any counters/maps/lists used. 
   */
  public abstract void reset();    
  
  /**
   * Produce a key-value map of logged values. The content
   * of the returned map is context dependent. 
   */
  public abstract Map<String, Object> getLoggedValues(int myId);
}
