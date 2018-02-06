package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

public class SequentialStrategy<ResourcePoolT extends ResourcePool> implements
    BatchEvaluationStrategy<ResourcePoolT> {

  @Override
  public void processBatch(
      ProtocolCollection<ResourcePoolT> protocols, ResourcePoolT resourcePool,
      NetworkBatchDecorator networkBatchDecorator) {
    for (NativeProtocol<?, ResourcePoolT> protocol : protocols) {
      int round = 0;
      EvaluationStatus status;
      do {
        status = protocol.evaluate(round, resourcePool, networkBatchDecorator);
        // send phase
        networkBatchDecorator.flush();
        round++;
      } while (status.equals(EvaluationStatus.HAS_MORE_ROUNDS));
    }
  }

}
