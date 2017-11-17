package dk.alexandra.fresco.logging;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NetworkLoggingDecorator implements Network, PerformanceLogger, Closeable {

  private Network delegate;
  private ConcurrentMap<Integer, Pair<Integer, Integer>> networkLogger = new ConcurrentHashMap<>();
  private int minBytesReceived = Integer.MAX_VALUE;
  private int maxBytesReceived = 0;

  public NetworkLoggingDecorator(Network network) {
    this.delegate = network;
  }

  @Override
  public byte[] receive(int partyId) {
    byte[] res = this.delegate.receive(partyId);
    int noBytes = res.length;
    if (!networkLogger.containsKey(partyId)) {
      networkLogger.put(partyId, new Pair<>(1, noBytes));
    } else {
      Pair<Integer, Integer> p = networkLogger.get(partyId);
      networkLogger.put(partyId, new Pair<>(p.getFirst() + 1, p.getSecond() + noBytes));
    }

    if (minBytesReceived > noBytes) {
      minBytesReceived = noBytes;
    }
    if (maxBytesReceived < noBytes) {
      maxBytesReceived = noBytes;
    }
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
    if (networkLogger.isEmpty()) {
      log.info("No network activity logged");
    } else {
      long totalNoBytes = 0;
      int noNetworkBatches = 0;
      for (Integer partyId : networkLogger.keySet()) {
        Pair<Integer, Integer> p = networkLogger.get(partyId);
        log.info("Received " + p.getSecond() + " bytes from party " + partyId);
        totalNoBytes += p.getSecond();
        noNetworkBatches += p.getFirst();
      }
      log.info("Received data " + noNetworkBatches + " times in total (including from ourselves)");
      log.info("Total amount of bytes received: " + totalNoBytes);
      log.info("Minimum amount of bytes received: " + minBytesReceived);
      log.info("maximum amount of bytes received: " + maxBytesReceived);
      double avg = totalNoBytes / (double) noNetworkBatches;
      log.info("Average amount of bytes received: " + df.format(avg));
    }
  }

  @Override
  public void reset() {
    networkLogger.clear();
    minBytesReceived = Integer.MAX_VALUE;
    maxBytesReceived = 0;
  }

  @Override
  public void close() throws IOException {
    if (delegate instanceof Closeable) {
      ((Closeable) delegate).close();
    }
  }
}
