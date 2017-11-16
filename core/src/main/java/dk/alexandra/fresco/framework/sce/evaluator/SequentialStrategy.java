package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.SceNetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.io.IOException;

public class SequentialStrategy<ResourcePoolT extends ResourcePool> implements
    BatchEvaluationStrategy<ResourcePoolT> {

  @Override
  public void processBatch(
      ProtocolCollection<ResourcePoolT> protocols, ResourcePoolT resourcePool,
      SceNetwork sceNetwork)
      throws IOException {
    Network network = resourcePool.getNetwork();
    for (NativeProtocol<?, ResourcePoolT> protocol : protocols) {
      int round = 0;
      EvaluationStatus status;
      do {
        status = protocol.evaluate(round, resourcePool, sceNetwork);
        // send phase
        sceNetwork.flush();
        round++;
      } while (status.equals(EvaluationStatus.HAS_MORE_ROUNDS));
    }
  }

}
