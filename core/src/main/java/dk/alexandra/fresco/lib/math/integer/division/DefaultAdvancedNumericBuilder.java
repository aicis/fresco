package dk.alexandra.fresco.lib.math.integer.division;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.AdvancedNumericBuilder;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;

public class DefaultAdvancedNumericBuilder implements
    AdvancedNumericBuilder {

  private final BuilderFactoryNumeric factoryNumeric;
  private final ProtocolBuilder builder;

  public DefaultAdvancedNumericBuilder(BuilderFactoryNumeric factoryNumeric,
      ProtocolBuilder builder) {
    this.factoryNumeric = factoryNumeric;
    this.builder = builder;
  }


  @Override
  public Computation<SInt> div(Computation<SInt> dividend, OInt divisor) {
    return builder
        .createSequentialSub(new KnownDivisorProtocol4(factoryNumeric, dividend, divisor));
  }

  @Override
  public Computation<SInt> remainder(Computation<SInt> dividend, OInt divisor) {
    return builder.createSequentialSub(new KnownDivisorRemainderProtocol4(dividend, divisor));
  }

  @Override
  public Computation<SInt> div(Computation<SInt> dividend, Computation<SInt> divisor) {

    return null;
  }

  @Override
  public Computation<SInt> div(Computation<SInt> dividend, Computation<SInt> divisor,
      OInt precision) {
    return null;
  }
}
