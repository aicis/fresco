package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;

public class MarlinProtocolSuite implements ProtocolSuiteNumeric<MarlinResourcePool> {

  @Override
  public BuilderFactoryNumeric init(MarlinResourcePool resourcePool, Network network) {
    return null;
  }

  @Override
  public RoundSynchronization<MarlinResourcePool> createRoundSynchronization() {
    return new MarlinRoundSynchronization();
  }

}
