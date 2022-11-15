package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTResourcePool;

public class CRTProtocolSuite<A extends NumericResourcePool, B extends NumericResourcePool> implements
    ProtocolSuiteNumeric<CRTResourcePool<A, B>> {

  private final BuilderFactoryNumeric left;
  private final BuilderFactoryNumeric right;

  public CRTProtocolSuite(BuilderFactoryNumeric left,
                          BuilderFactoryNumeric right) {
    this.left = left;
    this.right = right;
  }

  public BuilderFactoryNumeric getLeft() {
    return left;
  }

  public BuilderFactoryNumeric getRight() {
    return right;
  }

  @Override
  public BuilderFactoryNumeric init(CRTResourcePool<A, B> resourcePool) {
    Pair<A, B> resourcePools = resourcePool.getSubResourcePools();
    return new CRTBuilderFactory<>(resourcePools.getFirst(), left, resourcePools.getSecond(),
        right);
  }

  @Override
  public RoundSynchronization<CRTResourcePool<A, B>> createRoundSynchronization() {
    return new RoundSynchronization<CRTResourcePool<A, B>>() {
      @Override
      public void beforeBatch(ProtocolCollection<CRTResourcePool<A, B>> nativeProtocols,
          CRTResourcePool<A, B> resourcePool, Network network) {
      }

      @Override
      public void finishedBatch(int gatesEvaluated, CRTResourcePool<A, B> resourcePool,
          Network network) {
      }

      @Override
      public void finishedEval(CRTResourcePool<A, B> resourcePool, Network network) {
      }
    };
  }
}