package dk.alexandra.fresco.lib.math.integer.division;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.AdvancedNumericBuilder;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.conversion.IntegerToBitsByShiftProtocolImpl4;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationProtocol4;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationProtocolOpenBase;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationProtocolOpenExponent;
import java.util.List;

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
  public Computation<SInt> mod(Computation<SInt> dividend, OInt divisor) {
    return builder.createSequentialSub(new KnownDivisorRemainderProtocol4(dividend, divisor));
  }

  @Override
  public Computation<SInt> div(Computation<SInt> dividend, Computation<SInt> divisor) {
    return builder.createSequentialSub(
        new SecretSharedDivisorProtocol4(dividend, divisor, factoryNumeric)
    );
  }

  @Override
  public Computation<List<SInt>> toBits(Computation<SInt> in, int maxInputLength) {
    return builder.createSequentialSub(new IntegerToBitsByShiftProtocolImpl4(in, maxInputLength));
  }

  @Override
  public Computation<SInt> exp(Computation<SInt> x, Computation<SInt> e, int maxExponentLength) {
    return builder.createSequentialSub(new ExponentiationProtocol4(x, e, maxExponentLength));
  }

  @Override
  public Computation<SInt> exp(OInt x, Computation<SInt> e, int maxExponentLength) {
    return builder.createSequentialSub(new ExponentiationProtocolOpenBase(x, e, maxExponentLength));
  }

  @Override
  public Computation<SInt> exp(Computation<SInt> x, OInt e) {
    return builder.createSequentialSub(new ExponentiationProtocolOpenExponent(x, e));
  }
}
