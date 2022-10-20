package dk.alexandra.fresco.suite.crt.protocols.framework;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.NetworkBatchDecorator;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTResourcePool;

public class CRTSequentialStrategy<ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool> implements
        BatchEvaluationStrategy<CRTResourcePool<ResourcePoolA, ResourcePoolB>> {

  @Override
  public void processBatch(ProtocolCollection<CRTResourcePool<ResourcePoolA, ResourcePoolB>> protocols,
                           CRTResourcePool<ResourcePoolA, ResourcePoolB> resourcePool,
                           NetworkBatchDecorator network) {
    for (NativeProtocol<?, CRTResourcePool<ResourcePoolA, ResourcePoolB>> protocol : protocols) {
      int round = 0;
      NativeProtocol.EvaluationStatus status;
      do {
        status = protocol.evaluate(round, resourcePool, network);
        // send phase
        network.flush();
        round++;
      } while (status.equals(NativeProtocol.EvaluationStatus.HAS_MORE_ROUNDS));
    }
  }
}