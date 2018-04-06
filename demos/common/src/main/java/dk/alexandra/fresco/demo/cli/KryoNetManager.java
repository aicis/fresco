package dk.alexandra.fresco.demo.cli;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class KryoNetManager implements Closeable {

  private final List<Closeable> openedNetworks;
  private final NetworkConfiguration networkConfiguration;
  private final int offsetIncrement;
  private int portOffset;

  public KryoNetManager(NetworkConfiguration networkConfiguration, int offsetIncrement) {
    this.openedNetworks = new ArrayList<>();
    this.networkConfiguration = networkConfiguration;
    this.offsetIncrement = offsetIncrement;
    this.portOffset = 0;
  }

  public synchronized KryoNetNetwork createNetwork() {
    KryoNetNetwork net = new KryoNetNetwork(offsetNetworkConfiguration(portOffset));
    portOffset += offsetIncrement;
    openedNetworks.add(net);
    return net;
  }

  private NetworkConfiguration offsetNetworkConfiguration(int offset) {
    final Map<Integer, Party> parties = new HashMap<>();
    for (int id = 1; id <= networkConfiguration.noOfParties(); id++) {
      Party other = this.networkConfiguration.getParty(id);
      Party party = new Party(other.getPartyId(), other.getHostname(),
          other.getPort() + offset, other.getSecretSharedKey());
      parties.put(id, party);
    }

    return new NetworkConfigurationImpl(this.networkConfiguration.getMyId(), parties);
  }

  @Override
  public void close() {
    openedNetworks.forEach(this::close);
  }

  private void close(Closeable closeable) {
    ExceptionConverter.safe(() -> {
      closeable.close();
      return null;
    }, "IO Exception");
  }
}
