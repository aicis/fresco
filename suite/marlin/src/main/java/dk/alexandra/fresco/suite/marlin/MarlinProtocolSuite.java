package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUIntFactory;

public class MarlinProtocolSuite<T extends BigUInt<T>> implements
    ProtocolSuiteNumeric<MarlinResourcePool> {

  private final BigUIntFactory<T> factory;

  public MarlinProtocolSuite(BigUIntFactory<T> factory) {
    this.factory = factory;
  }

  @Override
  public BuilderFactoryNumeric init(MarlinResourcePool resourcePool, Network network) {
    return null;
  }

  @Override
  public RoundSynchronization<MarlinResourcePool> createRoundSynchronization() {
    return new MarlinRoundSynchronization();
  }

}
