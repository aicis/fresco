package dk.alexandra.fresco.framework.network;

import dk.alexandra.fresco.framework.PerformanceLogger;
import dk.alexandra.fresco.framework.PerformanceLogger.Flag;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import java.io.IOException;

public class NetworkPerformanceDecorator implements Network {

  private Network network;
  private PerformanceLogger pl;

  public NetworkPerformanceDecorator(Network network, PerformanceLogger pl) {
    super();
    this.network = network;
    this.pl = pl;
  }

  @Override
  public byte[] receive(int channelId, int partyId) throws IOException {
    byte[] res = this.network.receive(channelId, partyId);
    if (pl.flags.contains(Flag.LOG_NETWORK)) {
      pl.bytesReceived(res.length, partyId);
    }
    return res;
  }

  @Override
  public void init(NetworkConfiguration conf, int channelAmount) {
    this.network.init(conf, channelAmount);
  }

  @Override
  public void connect(int timeoutMillis) throws IOException {
    this.network.connect(timeoutMillis);
  }

  @Override
  public void send(int channelId, int partyId, byte[] data) throws IOException {
    this.network.send(channelId, partyId, data);
  }

  @Override
  public void close() throws IOException {
    this.network.close();
  }
}
