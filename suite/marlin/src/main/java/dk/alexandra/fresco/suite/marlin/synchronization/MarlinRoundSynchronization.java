package dk.alexandra.fresco.suite.marlin.synchronization;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.NetworkBatchDecorator;
import dk.alexandra.fresco.framework.sce.evaluator.ProtocolCollectionList;
import dk.alexandra.fresco.suite.ProtocolSuite.RoundSynchronization;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStore;

public class MarlinRoundSynchronization<T extends BigUInt<T>> implements
    RoundSynchronization<MarlinResourcePool<T>> {

  private final int openValueThreshold;
  private final int batchSize;
  private boolean isCheckRequired;

  public MarlinRoundSynchronization() {
    this(10000, 128);
  }

  public MarlinRoundSynchronization(int openValueThreshold, int batchSize) {
    this.openValueThreshold = openValueThreshold;
    this.batchSize = batchSize;
    this.isCheckRequired = false;
  }

  private void doMacCheck(MarlinResourcePool<T> resourcePool, Network network) {
    NetworkBatchDecorator networkBatchDecorator =
        new NetworkBatchDecorator(
            resourcePool.getNoOfParties(),
            network);
    MarlinOpenedValueStore<T> openedValueStore = resourcePool.getOpenedValueStore();
    if (!openedValueStore.isEmpty()) {
      MarlinMacCheckProtocolProducer<T> macCheck = new MarlinMacCheckProtocolProducer<>(
          resourcePool);
      do {
        ProtocolCollectionList<MarlinResourcePool> protocolCollectionList =
            new ProtocolCollectionList<>(batchSize);
        macCheck.getNextProtocols(protocolCollectionList);
        new BatchedStrategy<MarlinResourcePool>()
            .processBatch(protocolCollectionList, resourcePool, networkBatchDecorator);
      } while (macCheck.hasNextProtocols());
    }
  }

  @Override
  public void finishedBatch(int gatesEvaluated, MarlinResourcePool<T> resourcePool,
      Network network) {
  }

  @Override
  public void finishedEval(MarlinResourcePool<T> resourcePool, Network network) {
    doMacCheck(resourcePool, network);
  }

  @Override
  public void beforeBatch(ProtocolCollection<MarlinResourcePool<T>> nativeProtocols,
      MarlinResourcePool<T> resourcePool, Network network) {
//    for (NativeProtocol<?, ?> protocol : nativeProtocols) {
//      if (protocol instanceof MarlinOutputProtocol) {
//        isCheckRequired = true;
//        break;
//      }
//    }
//    if (isCheckRequired) {
//      System.out.println("Check required");
//      doMacCheck(resourcePool, network);
//    }
  }
}
