package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUIntFactory;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.synchronization.MarlinRoundSynchronization;

public class MarlinProtocolSuite<T extends BigUInt<T>> implements
    ProtocolSuiteNumeric<MarlinResourcePool> {

  private final BigUIntFactory<T> factory;

  MarlinProtocolSuite(BigUIntFactory<T> factory) {
    this.factory = factory;
  }

  @Override
  public BuilderFactoryNumeric init(MarlinResourcePool resourcePool, Network network) {
    BasicNumericContext numericContext = new BasicNumericContext(
        resourcePool.getEffectiveBitLength(), resourcePool.getModulus(), resourcePool.getMyId(),
        resourcePool.getNoOfParties());
    return new MarlinBuilder<>(factory, numericContext);
  }

  @Override
  public RoundSynchronization<MarlinResourcePool> createRoundSynchronization() {
    return new MarlinRoundSynchronization();
  }

}
