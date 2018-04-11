package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.RealNumericContext;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;

public class SpdzProtocolSuite implements ProtocolSuiteNumeric<SpdzResourcePool> {

  private final int maxBitLength;
  private final int fixedPointPrecision;
  private final boolean useBatchedBuilder;

  public SpdzProtocolSuite(int maxBitLength, int fixedPointPrecision, boolean useBatchedBuilder) {
    this.maxBitLength = maxBitLength;
    this.fixedPointPrecision = fixedPointPrecision;
    this.useBatchedBuilder = useBatchedBuilder;
  }

  public SpdzProtocolSuite(int maxBitLength, int fixedPointPrecision) {
    this(maxBitLength, fixedPointPrecision, false);
  }

  public SpdzProtocolSuite(int maxBitLength, boolean useBatchedBuilder) {
    this(maxBitLength, maxBitLength / 8, useBatchedBuilder);
  }

  public SpdzProtocolSuite(int maxBitLength) {
    this(maxBitLength, maxBitLength / 8);
  }

  @Override
  public BuilderFactoryNumeric init(SpdzResourcePool resourcePool, Network network) {
    BasicNumericContext numericContext = createNumericContext(resourcePool);
    RealNumericContext realContext = createRealNumericContext();
    if (useBatchedBuilder) {
      return new SpdzBatchedBuilder(numericContext, realContext);
    } else {
      return new SpdzBuilder(numericContext, realContext);
    }
  }

  BasicNumericContext createNumericContext(SpdzResourcePool resourcePool) {
    return new BasicNumericContext(maxBitLength, resourcePool.getModulus(),
        resourcePool.getMyId(), resourcePool.getNoOfParties());
  }

  RealNumericContext createRealNumericContext() {
    return new RealNumericContext(fixedPointPrecision);
  }

  @Override
  public RoundSynchronization<SpdzResourcePool> createRoundSynchronization() {
    return new SpdzRoundSynchronization(this);
  }

}
