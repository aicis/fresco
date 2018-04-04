package dk.alexandra.fresco.suite.spdz.maccheck;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.NetworkBatchDecorator;
import dk.alexandra.fresco.framework.sce.evaluator.ProtocolCollectionList;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzRoundSynchronization;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;
import java.security.SecureRandom;

public class MaliciousSpdzRoundSynchronization extends SpdzRoundSynchronization {

  public MaliciousSpdzRoundSynchronization(SpdzProtocolSuite spdzProtocolSuite) {
    super(spdzProtocolSuite);
  }

  @Override
  protected void doMacCheck(SpdzResourcePool resourcePool, Network network) {
    NetworkBatchDecorator networkBatchDecorator =
        new NetworkBatchDecorator(resourcePool.getNoOfParties(), network);
    OpenedValueStore<SpdzSInt, BigInteger> store = resourcePool.getOpenedValueStore();
    MaliciousSpdzMacCheckProtocol macCheck = new MaliciousSpdzMacCheckProtocol(new SecureRandom(),
        resourcePool.getMessageDigest(),
        store.popValues(),
        resourcePool.getModulus(),
        resourcePool.getRandomGenerator(),
        resourcePool.getDataSupplier().getSecretSharedKey());
    do {
      ProtocolCollectionList<SpdzResourcePool> protocolCollectionList =
          new ProtocolCollectionList<>(getBatchSize());
      macCheck.getNextProtocols(protocolCollectionList);
      BatchEvaluationStrategy<SpdzResourcePool> batchStrat = new BatchedStrategy<>();
      batchStrat.processBatch(protocolCollectionList, resourcePool, networkBatchDecorator);
    } while (macCheck.hasNextProtocols());
  }
}
