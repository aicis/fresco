package dk.alexandra.fresco.suite.spdz.maccheck;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.NetworkBatchDecorator;
import dk.alexandra.fresco.framework.sce.evaluator.ProtocolCollectionList;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzRoundSynchronization;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import java.security.SecureRandom;

public class MaliciousSpdzRoundSynchronization extends SpdzRoundSynchronization {

  @Override
  protected void doMacCheck(SpdzResourcePool resourcePool, Network network) {
    NetworkBatchDecorator networkBatchDecorator =
        new NetworkBatchDecorator(resourcePool.getNoOfParties(), network);
    SpdzStorage storage = resourcePool.getStore();
    int batchSize = 128;

    // Ensure that we have any values to do MAC check on
    if (!storage.getOpenedValues().isEmpty()) {
      MaliciousSpdzMacCheckProtocol macCheck = new MaliciousSpdzMacCheckProtocol(new SecureRandom(),
          resourcePool.getMessageDigest(), storage, resourcePool.getModulus());

      do {
        ProtocolCollectionList<SpdzResourcePool> protocolCollectionList =
            new ProtocolCollectionList<>(batchSize);
        macCheck.getNextProtocols(protocolCollectionList);
        BatchEvaluationStrategy<SpdzResourcePool> batchStrat = new BatchedStrategy<>();
        batchStrat.processBatch(protocolCollectionList, resourcePool, networkBatchDecorator);
      } while (macCheck.hasNextProtocols());
    }
  }
}
