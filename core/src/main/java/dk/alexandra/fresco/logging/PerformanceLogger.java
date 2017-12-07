package dk.alexandra.fresco.logging;

import java.util.Map;
import java.util.Map.Entry;
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
  
  /**
   * Prints any performance numbers picked up using the
   * default logger.
   */
  public default void printPerformanceLog(int myId){
    log.info(makeLogString(myId));
  }
  
  /**
   * Resets any counters/maps/lists used. 
   */
  public abstract void reset();    
  
  /**
   * Produce a key-value map of logged values. The content
   * of the returned map is context dependent. 
   */
  public abstract Map<String, Object> getLoggedValues(int myId);
  
  /**
   * Make a generic log string from the logged values. 
   */
  public default String makeLogString(int myId){
    String s = "Logger for "+this.getClass().getName()+": ";
    for (Entry<String, Object> e : getLoggedValues(myId).entrySet()) {
      s += "["+e.getKey().toString() + ": " + e.getValue().toString()+"]";
    }
    return s;
  }
}
