package dk.alexandra.fresco.framework;

import java.text.DecimalFormat;
import java.util.EnumSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for getting performance numbers. Use the EnumSet to indicate which parameters to log. Note
 * that network logging is done on ONLY the data received and used within FRESCO. This means that if
 * the network implementation uses double the bytes to wrap the messages, this will not show.
 * 
 * @author Kasper Damgaard
 *
 */
public abstract class PerformanceLogger {

  protected Logger log = LoggerFactory.getLogger(PerformanceLogger.class);
  protected DecimalFormat df = new DecimalFormat("#.00");
  
  public enum Flag {
    LOG_NETWORK, LOG_RUNTIME, LOG_NATIVE_BATCH;

    public static final EnumSet<Flag> ALL_OPTS = EnumSet.allOf(Flag.class);
  }

  protected final int myId;
  protected int counter = 0;

  public PerformanceLogger(int myId) {
    this.myId = myId;
  }

  /**
   * Prints any performance numbers picked up.
   */
  public abstract void printPerformanceLog();
  
  /**
   * Resets any counters/maps/lists used. 
   */
  public abstract void reset();    
  
}
