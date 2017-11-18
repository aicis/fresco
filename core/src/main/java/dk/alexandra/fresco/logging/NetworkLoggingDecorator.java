package dk.alexandra.fresco.logging;

import dk.alexandra.fresco.framework.network.Network;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NetworkLoggingDecorator implements Network, PerformanceLogger, Closeable {

  private Network delegate;
  private Map<Integer, PartyStats> partyStatsMap = new HashMap<>();
  private int minBytesReceived = Integer.MAX_VALUE;
  private int maxBytesReceived = 0;

  public NetworkLoggingDecorator(Network network) {
    this.delegate = network;
  }

  @Override
  public byte[] receive(int partyId) {
    byte[] res = this.delegate.receive(partyId);
    int noBytes = res.length;
    partyStatsMap.computeIfAbsent(partyId, (i) -> new PartyStats()).recordTransmission(noBytes);
    minBytesReceived = Math.min(noBytes, minBytesReceived);
    maxBytesReceived = Math.max(noBytes, maxBytesReceived);
    return res;
  }

  @Override
  public int getNoOfParties() {
    return delegate.getNoOfParties();
  }

  @Override
  public void send(int partyId, byte[] data) {
    this.delegate.send(partyId, data);
  }

  @Override
  public void printPerformanceLog(int myId) {
    log.info("=== P" + myId + ": Network logged - results ===");
    long totalNoBytes = 0;
    int noNetworkBatches = 0;
    for (Integer partyId : partyStatsMap.keySet()) {
      PartyStats partyStats = partyStatsMap.get(partyId);
      log.info("Received " + partyStats.noBytes + " bytes from party " + partyId);
      totalNoBytes += partyStats.noBytes;
      noNetworkBatches += partyStats.count;
    }
    log.info("Received data " + noNetworkBatches + " times in total (including from ourselves)");
    log.info("Total amount of bytes received: " + totalNoBytes);
    log.info("Minimum amount of bytes received: " + minBytesReceived);
    log.info("maximum amount of bytes received: " + maxBytesReceived);
    double avg = totalNoBytes / (double) noNetworkBatches;
    log.info("Average amount of bytes received: " + df.format(avg));
  }

  @Override
  public void reset() {
    partyStatsMap.clear();
    minBytesReceived = Integer.MAX_VALUE;
    maxBytesReceived = 0;
  }

  @Override
  public void close() throws IOException {
    if (delegate instanceof Closeable) {
      ((Closeable) delegate).close();
    }
  }

  private class PartyStats {

    private int count;
    private int noBytes;

    public void recordTransmission(int noBytes) {
      this.count++;
      this.noBytes += noBytes;
    }
  }
}
