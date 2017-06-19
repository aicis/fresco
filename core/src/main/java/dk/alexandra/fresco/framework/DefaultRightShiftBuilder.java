package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftProtocol4;

public class DefaultRightShiftBuilder<SIntT extends SInt> implements RightShiftBuilder<SIntT> {

  private final BuilderFactoryNumeric<SIntT> factoryNumeric;
  private final ProtocolBuilder<SIntT> builder;
  private SIntT TODO = null;

  DefaultRightShiftBuilder(
      BuilderFactoryNumeric<SIntT> factoryNumeric,
      ProtocolBuilder<SIntT> builder) {
    this.factoryNumeric = factoryNumeric;
    this.builder = builder;
  }

  @Override
  public Computation<SIntT> rightShift(Computation<SIntT> input) {
    return builder.append(
        new RightShiftProtocol4<>(
            factoryNumeric,
            factoryNumeric.getBasicNumericFactory().getMaxBitLength(),
            input));
  }

  @Override
  public Computation<SIntT> rightShift(Computation<SIntT> input, int shifts) {
    return () -> TODO;
  }

  @Override
  public Computation<RightShiftResult<SIntT>> rightShiftWithRemainder(
      Computation<SIntT> input,
      int shifts) {
    return () -> null;
  }
}
