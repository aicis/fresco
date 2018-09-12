package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.RealNumericContext;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;

public class SpdzProtocolSuite implements ProtocolSuiteNumeric<SpdzResourcePool> {

  private static final int DEFAULT_STATISTICAL_SECURITY = 40;
  private final int maxBitLength;
  private final int fixedPointPrecision;
  private final int statisticalSecurityParam;

  public SpdzProtocolSuite(int maxBitLength, int fixedPointPrecision,
      int statisticalSecurityParam) {
    this.maxBitLength = maxBitLength;
    this.fixedPointPrecision = fixedPointPrecision;
    this.statisticalSecurityParam = statisticalSecurityParam;
  }

  public SpdzProtocolSuite(int maxBitLength) {
    this(maxBitLength, maxBitLength / 8, DEFAULT_STATISTICAL_SECURITY);
  }

  @Override
  public BuilderFactoryNumeric init(SpdzResourcePool resourcePool, Network network) {
    BasicNumericContext numericContext = createNumericContext(resourcePool);
    RealNumericContext realContext = createRealNumericContext();
    return new SpdzBuilder(numericContext, realContext);
  }

  BasicNumericContext createNumericContext(SpdzResourcePool resourcePool) {
    return new BasicNumericContext(maxBitLength, resourcePool.getModulus(),
        resourcePool.getMyId(), resourcePool.getNoOfParties(), statisticalSecurityParam);
  }

  RealNumericContext createRealNumericContext() {
    return new RealNumericContext(fixedPointPrecision);
  }

  @Override
  public RoundSynchronization<SpdzResourcePool> createRoundSynchronization() {
    return new SpdzRoundSynchronization(this);
  }

}
