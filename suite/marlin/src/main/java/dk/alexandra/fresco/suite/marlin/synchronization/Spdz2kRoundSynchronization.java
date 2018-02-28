package dk.alexandra.fresco.suite.marlin.synchronization;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.suite.ProtocolSuite.RoundSynchronization;
import dk.alexandra.fresco.suite.marlin.Spdz2kBuilder;
import dk.alexandra.fresco.suite.marlin.Spdz2kProtocolSuite;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntConverter;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.protocols.computations.Spdz2kMacCheckComputation;
import dk.alexandra.fresco.suite.marlin.protocols.natives.RequiresMacCheck;
import dk.alexandra.fresco.suite.marlin.resource.Spdz2kResourcePool;
import dk.alexandra.fresco.suite.marlin.resource.storage.Spdz2kOpenedValueStore;

/**
 * Round synchronization for SPDZ2k. <p>Requires a mac check to be performed on an all opened
 * unauthenticated values whenever an output protocol is encountered in a batch.</p>
 */
public class Spdz2kRoundSynchronization<
    HighT extends UInt<HighT>,
    LowT extends UInt<LowT>,
    PlainT extends CompUInt<HighT, LowT, PlainT>>
    implements RoundSynchronization<Spdz2kResourcePool<PlainT>> {

  private final int openValueThreshold;
  private final int batchSize;
  private boolean isCheckRequired;
  private final Spdz2kProtocolSuite<HighT, LowT, PlainT> protocolSuite;
  private final CompUIntConverter<HighT, LowT, PlainT> converter;

  public Spdz2kRoundSynchronization(Spdz2kProtocolSuite<HighT, LowT, PlainT> protocolSuite,
      CompUIntConverter<HighT, LowT, PlainT> converter) {
    this(protocolSuite, converter, 100000, 128);
  }

  public Spdz2kRoundSynchronization(Spdz2kProtocolSuite<HighT, LowT, PlainT> protocolSuite,
      CompUIntConverter<HighT, LowT, PlainT> converter,
      int openValueThreshold,
      int batchSize) {
    this.protocolSuite = protocolSuite;
    this.converter = converter;
    this.openValueThreshold = openValueThreshold;
    this.batchSize = batchSize;
    this.isCheckRequired = false;
  }

  private void doMacCheck(Spdz2kResourcePool<PlainT> resourcePool, Network network) {
    Spdz2kOpenedValueStore<PlainT> openedValueStore = resourcePool.getOpenedValueStore();
    if (!openedValueStore.isEmpty()) {
      Spdz2kBuilder<PlainT> builder = new Spdz2kBuilder<>(resourcePool.getFactory(),
          protocolSuite.createBasicNumericContext(resourcePool));
      BatchEvaluationStrategy<Spdz2kResourcePool<PlainT>> batchStrategy = new BatchedStrategy<>();
      BatchedProtocolEvaluator<Spdz2kResourcePool<PlainT>> evaluator = new BatchedProtocolEvaluator<>(
          batchStrategy,
          protocolSuite,
          batchSize);
      Spdz2kMacCheckComputation<HighT, LowT, PlainT> macCheck = new Spdz2kMacCheckComputation<>(
          resourcePool, converter);
      ProtocolBuilderNumeric sequential = builder.createSequential();
      macCheck.buildComputation(sequential);
      evaluator.eval(sequential.build(), resourcePool, network);
    }
  }

  @Override
  public void finishedBatch(int gatesEvaluated, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    if (isCheckRequired || resourcePool.getOpenedValueStore().size() > openValueThreshold) {
      doMacCheck(resourcePool, network);
      isCheckRequired = false;
    }
  }

  @Override
  public void finishedEval(Spdz2kResourcePool<PlainT> resourcePool, Network network) {
    doMacCheck(resourcePool, network);
  }

  @Override
  public void beforeBatch(
      ProtocolCollection<Spdz2kResourcePool<PlainT>> nativeProtocols,
      Spdz2kResourcePool<PlainT> resourcePool, Network network) {
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
