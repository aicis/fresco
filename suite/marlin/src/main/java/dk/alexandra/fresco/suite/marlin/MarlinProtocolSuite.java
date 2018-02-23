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

public class MarlinProtocolSuite<
    HighT extends UInt<HighT>,
    LowT extends UInt<LowT>,
    CompT extends CompUInt<HighT, LowT, CompT>>
    implements ProtocolSuiteNumeric<MarlinResourcePool<CompT>> {

  private final CompUIntConverter<HighT, LowT, CompT> converter;

  public MarlinProtocolSuite(CompUIntConverter<HighT, LowT, CompT> converter) {
    this.converter = converter;
  }

  @Override
  public BuilderFactoryNumeric init(MarlinResourcePool<CompT> resourcePool, Network network) {
    return new MarlinBuilder<>(resourcePool.getFactory(), createBasicNumericContext(resourcePool));
  }

  @Override
  public RoundSynchronization<MarlinResourcePool<CompT>> createRoundSynchronization() {
    return new MarlinRoundSynchronization<>(this, converter);
  }

  public BasicNumericContext createBasicNumericContext(MarlinResourcePool<CompT> resourcePool) {
    return new BasicNumericContext(
        resourcePool.getEffectiveBitLength(), resourcePool.getModulus(), resourcePool.getMyId(),
        resourcePool.getNoOfParties());
  }

}
