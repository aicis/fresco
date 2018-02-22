package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt128Factory;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePoolUInt128;

public class MarlinProtocolSuite128
    implements ProtocolSuiteNumeric<MarlinResourcePoolUInt128> {

  private final CompUInt128Factory factory;

  MarlinProtocolSuite128() {
    super();
    factory = new CompUInt128Factory();
  }

  @Override
  public BuilderFactoryNumeric init(MarlinResourcePoolUInt128 resourcePool, Network network) {
    return new MarlinBuilder<>(factory, createBasicNumericContext(resourcePool));
  }

  @Override
  public RoundSynchronization<MarlinResourcePoolUInt128> createRoundSynchronization() {
//    return new MarlinRoundSynchronization<>(this, factory);
    return null;
  }

  public BasicNumericContext createBasicNumericContext(MarlinResourcePoolUInt128 resourcePool) {
    return new BasicNumericContext(
        resourcePool.getEffectiveBitLength(), resourcePool.getModulus(), resourcePool.getMyId(),
        resourcePool.getNoOfParties());
  }

}
