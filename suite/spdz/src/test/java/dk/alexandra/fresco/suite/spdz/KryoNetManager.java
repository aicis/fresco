package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class KryoNetManager implements Closeable {

  private static final AtomicInteger PORT_OFFSET_COUNTER = new AtomicInteger(50);
  private static final int PORT_INCREMENT = 10;
  private final List<Integer> ports;
  private final List<Closeable> openedNetworks;
  private final int portOffset;

  public KryoNetManager(List<Integer> ports) {
    this.portOffset = PORT_OFFSET_COUNTER.addAndGet(PORT_INCREMENT);
    this.ports = ports;
    this.openedNetworks = new ArrayList<>();
  }

  public KryoNetNetwork createExtraNetwork(int myId) {
    KryoNetNetwork net = new KryoNetNetwork(new TestNetworkConfiguration(myId, ports, portOffset), 10485680, false, 15000);
    openedNetworks.add(net);
    return net;
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

  public static class TestNetworkConfiguration implements NetworkConfiguration {

    private final int myId;
    private final List<Integer> usedPorts;
    private final int portOffset;
    private final int noOfParties;

    private TestNetworkConfiguration(int myId, List<Integer> usedPorts, int portOffset) {
      this.myId = myId;
      this.usedPorts = usedPorts;
      this.portOffset = portOffset;
      this.noOfParties = usedPorts.size();
    }

    @Override
    public Party getParty(int id) {
      return new Party(id, "localhost", usedPorts.get(id - 1) + portOffset);
    }

    @Override
    public Party getMe() {
      return getParty(myId);
    }

    @Override
    public int getMyId() {
      return myId;
    }

    @Override
    public int noOfParties() {
      return noOfParties;
    }
  }
}
