package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
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

  public final boolean LOG_NETWORK; // Logs network traffic
  public final boolean LOG_RUNTIME; // Logs application runtimes
  public final boolean LOG_NATIVE_BATCH; // Logs metrics on native protocols used per batch

  private final int myId;

  // private variables for holding performance metric information
  private ConcurrentMap<Integer, Integer> networkLogger = new ConcurrentHashMap<>();
  private int noNetworkBatches = 0;
  private int minBytesReceived = Integer.MAX_VALUE;
  private int maxBytesReceived = 0;

  private List<RuntimeInfo> runtimeLogger = new ArrayList<>();

  private int noBatches = 0;
  private long noNativeProtocols = 0;
  private int minNoNativeProtocolsPerBatch = Integer.MAX_VALUE;
  private int maxNoNativeProtocolsPerBatch = 0;

  public PerformanceLogger(int partyId, boolean logNetwork, boolean logRunningTimes,
      boolean logNativePerBatch) {
    this.myId = partyId;
    this.LOG_NETWORK = logNetwork;
    this.LOG_RUNTIME = logRunningTimes;
    this.LOG_NATIVE_BATCH = logNativePerBatch;
  }

  // Methods for inputting performance data into the performance metric logger.

  /**
   * Informs the performance logger that an amount of bytes was received from the given party Id.
   * Subsequent calls to this method will increase the total amount received.
   * 
   * @param noBytes The number of bytes received.
   * @param fromPartyId The party the bytes where received from.
   */
  public void bytesReceivedInBatch(int noBytes, int fromPartyId) {
    if (!networkLogger.containsKey(fromPartyId)) {
      networkLogger.put(fromPartyId, noBytes);
    } else {
      networkLogger.put(fromPartyId, networkLogger.get(fromPartyId) + noBytes);
    }
    noNetworkBatches++;
    if (minBytesReceived > noBytes) {
      minBytesReceived = noBytes;
    }
    if (maxBytesReceived < noBytes) {
      maxBytesReceived = noBytes;
    }
  }

  /**
   * Informs the performance logger that an application took x ms to run
   * 
   * @param app The application which was run.
   * @param timeSpend The running time in ms.
   * @param protocolSuite The protocol suite used to compute the application.
   * @param strategy The strategy used to evalute the native protocols.
   */
  public void informRuntime(Application<?, ?> app, long timeSpend, EvaluationStrategy strategy,
      String protocolSuite) {
    this.runtimeLogger.add(new RuntimeInfo(app, timeSpend, strategy, protocolSuite));
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
    DecimalFormat df = new DecimalFormat("#.00");
    StringBuilder sb = new StringBuilder();
    newline("Performance metrics info log for party " + this.myId + ":", sb);

    if (LOG_RUNTIME) {
      newline("=== Running times for applications ===", sb);
      if (this.runtimeLogger.isEmpty()) {
        newline("No applications were run, or they have not completed yet.", sb);
      }
      for (RuntimeInfo info : this.runtimeLogger) {
        newline("Evaluation strategy used: " + info.strategy, sb);
        newline("Protocol suite used: " + info.protocolSuite, sb);
        newline(
            "The application " + info.app.getClass().getName() + " took " + info.timeSpend + "ms.",
            sb);
      }
      newline("", sb);
    }
    if (LOG_NETWORK) {
      newline("=== Network logged - results ===", sb);
      if (networkLogger.isEmpty()) {
        newline("No network activity logged", sb);
      } else {
        newline("Received data " + noNetworkBatches + " times in total (including from ourselves)",
            sb);
        long totalNoBytes = 0;
        for (Integer partyId : networkLogger.keySet()) {
          int bytes = networkLogger.get(partyId);
          newline("Received " + bytes + " bytes from party " + partyId, sb);
          totalNoBytes += bytes;
        }
        newline("Total amount of bytes received: " + totalNoBytes, sb);
        newline("Minimum amount of bytes received in a batch: " + minBytesReceived, sb);
        newline("maximum amount of bytes received in a batch: " + maxBytesReceived, sb);
        double avg = totalNoBytes / (double) noNetworkBatches;
        newline("Average amount of bytes received in a batch: " + df.format(avg), sb);
      }
      newline("", sb);
    }
    if (LOG_NATIVE_BATCH) {
      newline("=== Native protocols per batch metrics ===", sb);
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

  private class RuntimeInfo {
    public Application<?, ?> app;
    public long timeSpend;
    public EvaluationStrategy strategy;
    public String protocolSuite;

    public RuntimeInfo(Application<?, ?> app, long timeSpend, EvaluationStrategy strategy,
        String protocolSuite) {
      super();
      this.app = app;
      this.timeSpend = timeSpend;
      this.strategy = strategy;
      this.protocolSuite = protocolSuite;
    }

  }
}
