package dk.alexandra.fresco.suite.marlin.synchronization;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.gates.MarlinBroadcastProtocol;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import java.util.List;

public class MarlinMacCheckProtocolProducer<T extends BigUInt<T>> implements ProtocolProducer {

  private final SequentialProtocolProducer protocolProducer;

  public MarlinMacCheckProtocolProducer(MarlinResourcePool<T> resourcePool) {
    Pair<List<MarlinSInt<T>>, List<T>> opened = resourcePool.getOpenedValueStore().popValues();
    protocolProducer = new SequentialProtocolProducer(
        new MarlinBroadcastProtocol<T>()
    );
  }

  @Override
  public <ResourcePoolT extends ResourcePool> void getNextProtocols(
      ProtocolCollection<ResourcePoolT> protocolCollection) {

  }

  @Override
  public boolean hasNextProtocols() {
    return false;
  }
}
