package dk.alexandra.fresco.logging;

import dk.alexandra.fresco.framework.network.Network;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NetworkLoggingDecorator implements Network, PerformanceLogger, Closeable {

  public static final String NETWORK_PARTY_BYTES = "Amount of bytes received pr. party";
  public static final String NETWORK_TOTAL_BYTES = "Total amount of bytes received";
  public static final String NETWORK_TOTAL_BATCHES = "Total amount of batches received";

  private Network delegate;
  private Map<Integer, PartyStats> partyStatsMap = new HashMap<>();
  
  public NetworkLoggingDecorator(Network network) {
    this.delegate = network;
  }

  @Override
  public byte[] receive(int partyId) {
    byte[] res = this.delegate.receive(partyId);
    int noBytes = res.length;
    partyStatsMap.computeIfAbsent(partyId, (i) -> new PartyStats()).recordTransmission(noBytes);
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
  public void reset() {
    partyStatsMap.clear();
  }

  @Override
  public void close() throws IOException {
    if (delegate instanceof Closeable) {
      ((Closeable) delegate).close();
    }
  }

  private class PartyStats {
    private long count;
    private long noBytes;

    public void recordTransmission(int noBytes) {
      this.count++;
      this.noBytes += noBytes;
    }
  }

  @Override
  public Map<String, Long> getLoggedValues() {
    Map<String, Long> values = new HashMap<>();
    
    long totalNoBytes = 0;
    long noNetworkBatches = 0;
    for (Integer partyId : partyStatsMap.keySet()) {
      PartyStats partyStats = partyStatsMap.get(partyId);
      values.put(NETWORK_PARTY_BYTES + "_" + partyId, partyStats.noBytes);
      totalNoBytes += partyStats.noBytes;
      noNetworkBatches += partyStats.count;
    }
    values.put(NETWORK_TOTAL_BYTES, totalNoBytes);
    values.put(NETWORK_TOTAL_BATCHES, noNetworkBatches);
    return values;
  }

}
