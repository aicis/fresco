package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;

public class SpdzProtocolSuite implements ProtocolSuiteNumeric<SpdzResourcePool> {

  private final int maxBitLength;
  private final int fixedPointPrecision;

  public SpdzProtocolSuite(int maxBitLength, int fixedPointPrecision) {
    this.maxBitLength = maxBitLength;
    this.fixedPointPrecision = fixedPointPrecision;
  }

  public SpdzProtocolSuite(int maxBitLength) {
    this(maxBitLength, maxBitLength / 8);
  }

  @Override
  public BuilderFactoryNumeric init(SpdzResourcePool resourcePool) {
    BasicNumericContext numericContext = createNumericContext(resourcePool);
    return new SpdzBuilder(numericContext);
  }

  BasicNumericContext createNumericContext(SpdzResourcePool resourcePool) {
    return new BasicNumericContext(maxBitLength, resourcePool.getMyId(),
        resourcePool.getNoOfParties(), resourcePool.getFieldDefinition(),fixedPointPrecision);
  }

  @Override
  public RoundSynchronization<SpdzResourcePool> createRoundSynchronization() {
    return new SpdzRoundSynchronization(this);
  }
}
