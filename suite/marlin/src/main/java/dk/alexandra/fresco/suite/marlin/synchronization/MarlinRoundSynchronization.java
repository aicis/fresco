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
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntConverter;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.protocols.computations.MarlinMacCheckComputation;
import dk.alexandra.fresco.suite.marlin.protocols.natives.RequiresMacCheck;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStore;

public class MarlinRoundSynchronization<
    HighT extends UInt<HighT>,
    LowT extends UInt<LowT>,
    CompT extends CompUInt<HighT, LowT, CompT>>
    implements RoundSynchronization<MarlinResourcePool<CompT>> {

  private final int openValueThreshold;
  private final int batchSize;
  private boolean isCheckRequired;
  private final MarlinProtocolSuite<HighT, LowT, CompT> protocolSuite;
  private final CompUIntConverter<HighT, LowT, CompT> converter;

  public MarlinRoundSynchronization(MarlinProtocolSuite<HighT, LowT, CompT> protocolSuite,
      CompUIntConverter<HighT, LowT, CompT> converter) {
    this(protocolSuite, converter, 100000, 128);
  }

  public MarlinRoundSynchronization(MarlinProtocolSuite<HighT, LowT, CompT> protocolSuite,
      CompUIntConverter<HighT, LowT, CompT> converter,
      int openValueThreshold,
      int batchSize) {
    this.protocolSuite = protocolSuite;
    this.converter = converter;
    this.openValueThreshold = openValueThreshold;
    this.batchSize = batchSize;
    this.isCheckRequired = false;
  }

  private void doMacCheck(MarlinResourcePool<CompT> resourcePool, Network network) {
    MarlinOpenedValueStore<CompT> openedValueStore = resourcePool.getOpenedValueStore();
    if (!openedValueStore.isEmpty()) {
      MarlinBuilder<CompT> builder = new MarlinBuilder<>(resourcePool.getFactory(),
          protocolSuite.createBasicNumericContext(resourcePool));
      BatchEvaluationStrategy<MarlinResourcePool<CompT>> batchStrategy = new BatchedStrategy<>();
      BatchedProtocolEvaluator<MarlinResourcePool<CompT>> evaluator = new BatchedProtocolEvaluator<>(
          batchStrategy,
          protocolSuite,
          batchSize);
      MarlinMacCheckComputation<HighT, LowT, CompT> macCheck = new MarlinMacCheckComputation<>(
          resourcePool, converter);
      ProtocolBuilderNumeric sequential = builder.createSequential();
      macCheck.buildComputation(sequential);
      evaluator.eval(sequential.build(), resourcePool, network);
    }
  }

  @Override
  public void finishedBatch(int gatesEvaluated, MarlinResourcePool<CompT> resourcePool,
      Network network) {
    if (isCheckRequired || resourcePool.getOpenedValueStore().size() > openValueThreshold) {
      doMacCheck(resourcePool, network);
      isCheckRequired = false;
    }
  }

  @Override
  public void finishedEval(MarlinResourcePool<CompT> resourcePool, Network network) {
    doMacCheck(resourcePool, network);
  }

  @Override
  public void beforeBatch(
      ProtocolCollection<MarlinResourcePool<CompT>> nativeProtocols,
      MarlinResourcePool<CompT> resourcePool, Network network) {
    for (NativeProtocol<?, ?> protocol : nativeProtocols) {
      // TODO come up with a cleaner way of doing this
      if (protocol instanceof RequiresMacCheck) {
        isCheckRequired = true;
        break;
      }
    }
    if (isCheckRequired) {
      doMacCheck(resourcePool, network);
    }
  }

}
