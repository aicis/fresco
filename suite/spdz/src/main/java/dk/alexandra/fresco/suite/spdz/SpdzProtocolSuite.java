package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.security.SecureRandom;

public class SpdzProtocolSuite implements ProtocolSuite<SpdzResourcePool, ProtocolBuilderNumeric> {

  private final int maxBitLength;
  private final SecureRandom secRand;

  public SpdzProtocolSuite(int maxBitLength) {
    this.maxBitLength = maxBitLength;
    secRand = new SecureRandom();
  }

  @Override
  public BuilderFactory<ProtocolBuilderNumeric> init(SpdzResourcePool resourcePool,
      Network network) {
    BasicNumericContext spdzFactory =
        new BasicNumericContext(maxBitLength, resourcePool.getModulus(), resourcePool);
    return new SpdzBuilder(spdzFactory);
  }

  @Override
  public RoundSynchronization<SpdzResourcePool> createRoundSynchronization() {
    return new SpdzRoundSynchronization(secRand);
  }

}
