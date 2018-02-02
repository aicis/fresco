package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;

public class SpdzProtocolSuite implements ProtocolSuiteNumeric<SpdzResourcePool> {

  private final int maxBitLength;

  public SpdzProtocolSuite(int maxBitLength) {
    this.maxBitLength = maxBitLength;
  }

  @Override
  public BuilderFactoryNumeric init(SpdzResourcePool resourcePool, Network network) {
    BasicNumericContext spdzFactory =
        new BasicNumericContext(maxBitLength, resourcePool.getModulus(), resourcePool);
    return new SpdzBuilder(spdzFactory);
  }

  @Override
  public RoundSynchronization<SpdzResourcePool> createRoundSynchronization() {
    return new SpdzRoundSynchronization();
  }

}
