package dk.alexandra.fresco.logging;

import java.util.Map;

/**
 * Class for getting performance numbers. Use the EnumSet to indicate which parameters to log. Note
 * that network logging is done on ONLY the data received and used within FRESCO. This means that if
 * the network implementation uses double the bytes to wrap the messages, this will not show.
 *
 */
public interface PerformanceLogger {
  
  /**
   * Resets any counters/maps/lists used. 
   */
  public void reset();    
  
  /**
   * Produce a key-value map of logged values. The content
   * of the returned map is context dependent. 
   */
  public Map<String, Long> getLoggedValues(int myId);

}
