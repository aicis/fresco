package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.util.Iterator;

/**
 * This class implements the core of a general batched communication strategy for evaluating
 * Protocols.
 */
public class NativeBatchedStrategy<ResourcePoolT extends ResourcePool>
    implements BatchEvaluationStrategy<ResourcePoolT> {

  @Override
  public void processBatch(
      ProtocolCollection<ResourcePoolT> protocols, ResourcePoolT resourcePool,
      NetworkBatchDecorator network) {
    int round = 0;
    while (protocols.size() > 0) {
      evaluateCurrentRound(protocols, network.getDirectNetwork(), resourcePool, round);
      round++;
    }
  }

  private void evaluateCurrentRound(
      ProtocolCollection<ResourcePoolT> protocols, Network sceNetwork,
      ResourcePoolT rp, int round) {
    Iterator<NativeProtocol<?, ResourcePoolT>> iterator = protocols.iterator();
    while (iterator.hasNext()) {
      NativeProtocol<?, ResourcePoolT> protocol = iterator.next();
      EvaluationStatus status = protocol.evaluate(round, rp, sceNetwork);
      if (status.equals(EvaluationStatus.IS_DONE)) {
        iterator.remove();
      }
    }
  }
}
