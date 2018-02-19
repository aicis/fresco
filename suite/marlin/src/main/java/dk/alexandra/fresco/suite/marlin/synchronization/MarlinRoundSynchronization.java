package dk.alexandra.fresco.suite.marlin.synchronization;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.suite.ProtocolSuite.RoundSynchronization;
import dk.alexandra.fresco.suite.marlin.MarlinBuilder;
import dk.alexandra.fresco.suite.marlin.MarlinProtocolSuite;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUIntFactory;
import dk.alexandra.fresco.suite.marlin.protocols.computations.MarlinMacCheckComputation;
import dk.alexandra.fresco.suite.marlin.protocols.natives.MarlinOutputProtocol;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStore;

public class MarlinRoundSynchronization<T extends BigUInt<T>> implements
    RoundSynchronization<MarlinResourcePool<T>> {

  private final int openValueThreshold;
  private final int batchSize;
  private boolean isCheckRequired;
  private final BigUIntFactory<T> factory;
  private final MarlinProtocolSuite<T> protocolSuite;

  public MarlinRoundSynchronization(MarlinProtocolSuite<T> protocolSuite,
      BigUIntFactory<T> factory) {
    this(protocolSuite, factory, 100000, 128);
  }

  public MarlinRoundSynchronization(MarlinProtocolSuite<T> protocolSuite, BigUIntFactory<T> factory,
      int openValueThreshold,
      int batchSize) {
    this.factory = factory;
    this.protocolSuite = protocolSuite;
    this.openValueThreshold = openValueThreshold;
    this.batchSize = batchSize;
    this.isCheckRequired = false;
  }

  private void doMacCheck(MarlinResourcePool<T> resourcePool, Network network) {
    MarlinOpenedValueStore<T> openedValueStore = resourcePool.getOpenedValueStore();
    if (!openedValueStore.isEmpty()) {
      MarlinBuilder<T> builder = new MarlinBuilder<>(factory,
          protocolSuite.createBasicNumericContext(resourcePool));
      BatchEvaluationStrategy<MarlinResourcePool> batchStrategy = new BatchedStrategy<>();
      BatchedProtocolEvaluator<MarlinResourcePool> evaluator = new BatchedProtocolEvaluator<>(
          batchStrategy,
          protocolSuite,
          batchSize);
      MarlinMacCheckComputation<T> macCheck = new MarlinMacCheckComputation<>(resourcePool);
      ProtocolBuilderNumeric sequential = builder.createSequential();
      macCheck.buildComputation(sequential);
      evaluator.eval(sequential.build(), resourcePool, network);
    }
  }

  @Override
  public void finishedBatch(int gatesEvaluated, MarlinResourcePool<T> resourcePool,
      Network network) {
    if (isCheckRequired || resourcePool.getOpenedValueStore().size() > openValueThreshold) {
      doMacCheck(resourcePool, network);
      isCheckRequired = false;
    }
  }

  @Override
  public void finishedEval(MarlinResourcePool<T> resourcePool, Network network) {
    doMacCheck(resourcePool, network);
  }

  @Override
  public void beforeBatch(ProtocolCollection<MarlinResourcePool<T>> nativeProtocols,
      MarlinResourcePool<T> resourcePool, Network network) {
    for (NativeProtocol<?, ?> protocol : nativeProtocols) {
      if (protocol instanceof MarlinOutputProtocol) {
        isCheckRequired = true;
        break;
      }
    }
    if (isCheckRequired) {
      doMacCheck(resourcePool, network);
    }
  }

}
