package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.synchronization.MarlinRoundSynchronization;

public class MarlinProtocolSuite<H extends UInt<H>, L extends UInt<L>, T extends CompUInt<H, L, T>> implements
    ProtocolSuiteNumeric<MarlinResourcePool<H, L, T>> {

  private final CompUIntFactory<H, L, T> factory;

  MarlinProtocolSuite(CompUIntFactory<H, L, T> factory) {
    this.factory = factory;
  }

  @Override
  public BuilderFactoryNumeric init(MarlinResourcePool<H, L, T> resourcePool, Network network) {
    return new MarlinBuilder<>(factory, createBasicNumericContext(resourcePool));
  }

  @Override
  public RoundSynchronization<MarlinResourcePool<H, L, T>> createRoundSynchronization() {
    return new MarlinRoundSynchronization<>(this, factory);
  }

  public BasicNumericContext createBasicNumericContext(MarlinResourcePool<H, L, T> resourcePool) {
    return new BasicNumericContext(
        resourcePool.getEffectiveBitLength(), resourcePool.getModulus(), resourcePool.getMyId(),
        resourcePool.getNoOfParties());
  }

}
