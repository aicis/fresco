package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.util.Pair;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for getting performance numbers. Set final static boolean variables to true to log
 * performance numbers for the different parameters.
 * 
 * @author Kasper Damgaard
 *
 */
public class PerformanceLogger {

  private Logger log = LoggerFactory.getLogger(PerformanceLogger.class);

  private static final ConcurrentMap<Integer, PerformanceLogger> loggers =
      new ConcurrentHashMap<>();

  // static final booleans for which metrics to run
  // They have to be final to ensure that booleans set to false will not impede on performance.
  public static final boolean LOG_NETWORK = true; // Logs network traffic
  public static final boolean LOG_RUNTIME = true; // Logs application runtimes
  public static final boolean LOG_NATIVE_BATCH = true; // Logs metrics on native protocols used per
                                                       // batch

  private final int myId;

  // private variables for holding performance metric information
  private ConcurrentMap<Integer, Integer> networkLogger = new ConcurrentHashMap<>();
  private List<Pair<Application<?, ?>, Long>> runtimeLogger = new ArrayList<>();
  private int noBatches = 0;
  private long noNativeProtocols = 0;
  private int minNoNativeProtocolsPerBatch = Integer.MAX_VALUE;
  private int maxNoNativeProtocolsPerBatch = 0;

  private PerformanceLogger(int partyId) {
    this.myId = partyId;
  }

  public static PerformanceLogger getLogger(int partyId) {
    if (!loggers.containsKey(partyId)) {
      loggers.put(partyId, new PerformanceLogger(partyId));
    }
    return loggers.get(partyId);
  }

  // Methods for inputting performance data into the performance metric logger.

  /**
   * Informs the performance logger that an amount of bytes was received from the given party Id.
   * Subsequent calls to this method will increase the total amount received.
   * 
   * @param noBytes The number of bytes received.
   * @param fromPartyId The party the bytes where received from.
   */
  public void bytesReceived(int noBytes, int fromPartyId) {
    if (!networkLogger.containsKey(fromPartyId)) {
      networkLogger.put(fromPartyId, noBytes);
    } else {
      networkLogger.put(fromPartyId, networkLogger.get(fromPartyId) + noBytes);
    }
  }

  /**
   * Informs the performance logger that an application took x ms to run
   * 
   * @param app The application which was run.
   * @param timeSpend The running time in ms.
   */
  public void informRuntime(Application<?, ?> app, long timeSpend) {
    this.runtimeLogger.add(new Pair<>(app, timeSpend));
  }

  /**
   * Informs the performance logger that a batch of native protocols is going to be evaluated.
   * 
   * @param size The size of the batch - i.e. the number of native protocols within the batch.
   */
  public void nativeBatch(int size) {
    noBatches++;
    noNativeProtocols += size;
    if (minNoNativeProtocolsPerBatch > size) {
      minNoNativeProtocolsPerBatch = size;
    }
    if (maxNoNativeProtocolsPerBatch < size) {
      maxNoNativeProtocolsPerBatch = size;
    }
  }

  /**
   * Prints all performance metrics and resets counters, maps etc. for future runs.
   */
  public void printPerformanceLog() {
    StringBuilder sb = new StringBuilder();
    newline("Performance metrics info log for party " + this.myId + ":", sb);

    if (LOG_RUNTIME) {
      newline("Running times for applications:", sb);
      if (this.runtimeLogger.isEmpty()) {
        newline("No applications were run, or they have not completed yet.", sb);
      }
      for (Pair<Application<?, ?>, Long> p : this.runtimeLogger) {
        Application<?, ?> app = p.getFirst();
        long runtime = p.getSecond();
        newline("The application " + app.getClass().getName() + " took " + runtime + "ms.", sb);
      }
      newline("", sb);
    }
    if (LOG_NETWORK) {
      newline("Network logged - results:", sb);
      if (networkLogger.isEmpty()) {
        newline("No network activity logged", sb);
      } else {
        for (Integer partyId : networkLogger.keySet()) {
          newline("Received " + networkLogger.get(partyId) + " bytes from party " + partyId, sb);
        }
      }
      newline("", sb);
    }
    if (LOG_NATIVE_BATCH) {
      newline("Native protocols per batch metrics:", sb);
      if (noBatches == 0) {
        newline("No batches were recorded", sb);
      } else {
        newline("Total amount of batches reached: " + noBatches, sb);
        newline("Total amount of native protocols evaluated: " + noNativeProtocols, sb);
        newline("minimum amount of native protocols evaluated in a single batch: "
            + minNoNativeProtocolsPerBatch, sb);
        newline("maximum amount of native protocols evaluated in a single batch: "
            + maxNoNativeProtocolsPerBatch, sb);
        double avg = noNativeProtocols / (double) noBatches;
        DecimalFormat df = new DecimalFormat("#.00");
        newline("Average amount of native protocols evaluated per batch: " + df.format(avg), sb);
      }
      newline("", sb);
    }
    log.info(sb.toString());

    reset();
  }

  private void reset() {
    this.networkLogger.clear();
    this.runtimeLogger.clear();
  }

  private void newline(String msg, StringBuilder sb) {
    sb.append(msg);
    sb.append(System.lineSeparator());
  }
}
