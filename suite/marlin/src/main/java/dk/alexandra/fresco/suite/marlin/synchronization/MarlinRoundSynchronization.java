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
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.protocols.computations.MarlinMacCheckComputation;
import dk.alexandra.fresco.suite.marlin.protocols.natives.MarlinOutputProtocol;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStore;

public class MarlinRoundSynchronization<H extends UInt<H>, L extends UInt<L>, T extends CompUInt<H, L, T>> implements
    RoundSynchronization<MarlinResourcePool<H, L, T>> {

  private final int openValueThreshold;
  private final int batchSize;
  private boolean isCheckRequired;
  private final CompUIntFactory<H, L, T> factory;
  private final MarlinProtocolSuite<H, L, T> protocolSuite;

  public MarlinRoundSynchronization(MarlinProtocolSuite<H, L, T> protocolSuite,
      CompUIntFactory<H, L, T> factory) {
    this(protocolSuite, factory, 100000, 128);
  }

  public MarlinRoundSynchronization(MarlinProtocolSuite<H, L, T> protocolSuite,
      CompUIntFactory<H, L, T> factory,
      int openValueThreshold,
      int batchSize) {
    this.factory = factory;
    this.protocolSuite = protocolSuite;
    this.openValueThreshold = openValueThreshold;
    this.batchSize = batchSize;
    this.isCheckRequired = false;
  }

  private void doMacCheck(MarlinResourcePool<H, L, T> resourcePool, Network network) {
    MarlinOpenedValueStore<H, L, T> openedValueStore = resourcePool.getOpenedValueStore();
    if (!openedValueStore.isEmpty()) {
      MarlinBuilder<H, L, T> builder = new MarlinBuilder<>(factory,
          protocolSuite.createBasicNumericContext(resourcePool));
      BatchEvaluationStrategy<MarlinResourcePool<H, L, T>> batchStrategy = new BatchedStrategy<>();
      BatchedProtocolEvaluator<MarlinResourcePool<H, L, T>> evaluator = new BatchedProtocolEvaluator<>(
          batchStrategy,
          protocolSuite,
          batchSize);
      MarlinMacCheckComputation<H, L, T> macCheck = new MarlinMacCheckComputation<>(resourcePool);
      ProtocolBuilderNumeric sequential = builder.createSequential();
      macCheck.buildComputation(sequential);
      evaluator.eval(sequential.build(), resourcePool, network);
    }
  }

  @Override
  public void finishedBatch(int gatesEvaluated, MarlinResourcePool<H, L, T> resourcePool,
      Network network) {
    if (isCheckRequired || resourcePool.getOpenedValueStore().size() > openValueThreshold) {
      doMacCheck(resourcePool, network);
      isCheckRequired = false;
    }
  }

  @Override
  public void finishedEval(MarlinResourcePool<H, L, T> resourcePool, Network network) {
    doMacCheck(resourcePool, network);
  }

  @Override
  public void beforeBatch(ProtocolCollection<MarlinResourcePool<H, L, T>> nativeProtocols,
      MarlinResourcePool<H, L, T> resourcePool, Network network) {
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
