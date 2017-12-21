package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.ProtocolSuite;

public class SpdzProtocolSuite implements ProtocolSuite<SpdzResourcePool, ProtocolBuilderNumeric> {

  private final int maxBitLength;

  public SpdzProtocolSuite(int maxBitLength) {
    this.maxBitLength = maxBitLength;
  }

  @Override
  public BuilderFactory<ProtocolBuilderNumeric> init(SpdzResourcePool resourcePool,
      Network network) {
    BasicNumericContext numericContext =
        new BasicNumericContext(maxBitLength, resourcePool.getModulus(),
            resourcePool.getMyId(), resourcePool.getNoOfParties());
    return new SpdzBuilder(numericContext);
  }

  @Override
  public RoundSynchronization<SpdzResourcePool> createRoundSynchronization() {
    return new SpdzRoundSynchronization();
  }

}
