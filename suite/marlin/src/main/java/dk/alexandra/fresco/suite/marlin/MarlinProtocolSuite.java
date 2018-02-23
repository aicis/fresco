package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntConverter;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.synchronization.MarlinRoundSynchronization;

public abstract class MarlinProtocolSuite<
    HighT extends UInt<HighT>,
    LowT extends UInt<LowT>,
    PlainT extends CompUInt<HighT, LowT, PlainT>>
    implements ProtocolSuiteNumeric<MarlinResourcePool<PlainT>> {

  private final CompUIntConverter<HighT, LowT, PlainT> converter;

  public MarlinProtocolSuite(CompUIntConverter<HighT, LowT, PlainT> converter) {
    this.converter = converter;
  }

  @Override
  public BuilderFactoryNumeric init(MarlinResourcePool<PlainT> resourcePool, Network network) {
    return new MarlinBuilder<>(resourcePool.getFactory(), createBasicNumericContext(resourcePool));
  }

  @Override
  public RoundSynchronization<MarlinResourcePool<PlainT>> createRoundSynchronization() {
    return new MarlinRoundSynchronization<>(this, converter);
  }

  public BasicNumericContext createBasicNumericContext(MarlinResourcePool<PlainT> resourcePool) {
    return new BasicNumericContext(
        resourcePool.getMaxBitLength(), resourcePool.getModulus(), resourcePool.getMyId(),
        resourcePool.getNoOfParties());
  }

}
