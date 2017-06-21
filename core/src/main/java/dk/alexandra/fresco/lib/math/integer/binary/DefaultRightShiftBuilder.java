package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.RightShiftBuilder;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;

public class DefaultRightShiftBuilder<SIntT extends SInt> implements RightShiftBuilder<SIntT> {

  private final BuilderFactoryNumeric<SIntT> factoryNumeric;
  private final ProtocolBuilder<SIntT> builder;

  public DefaultRightShiftBuilder(
      BuilderFactoryNumeric<SIntT> factoryNumeric,
      ProtocolBuilder<SIntT> builder) {
    this.factoryNumeric = factoryNumeric;
    this.builder = builder;
  }

  @Override
  public Computation<SIntT> rightShift(Computation<SIntT> input) {
    Computation<RightShiftResult<SIntT>> rightShiftResult = builder
        .createSequentialSubFactoryReturning(
            new RightShiftProtocol4<>(
                factoryNumeric.getBasicNumericFactory().getMaxBitLength(),
                input, false));
    return () -> rightShiftResult.out().getResult().out();
  }

  @Override
  public Computation<RightShiftResult<SIntT>> rightShiftWithRemainder(Computation<SIntT> input) {
    return builder.createSequentialSubFactoryReturning(
        new RightShiftProtocol4<>(
            factoryNumeric.getBasicNumericFactory().getMaxBitLength(),
            input, true));
  }

  @Override
  public Computation<SIntT> rightShift(Computation<SIntT> input, int shifts) {
    Computation<RightShiftResult<SIntT>> rightShiftResult = builder
        .createSequentialSubFactoryReturning(
            new RepeatedRightShiftProtocol4<>(
                input, shifts, false));
    return () -> rightShiftResult.out().getResult().out();
  }

  @Override
  public Computation<RightShiftResult<SIntT>> rightShiftWithRemainder(
      Computation<SIntT> input,
      int shifts) {
    return builder.createSequentialSubFactoryReturning(
        new RepeatedRightShiftProtocol4<>(
            input, shifts, true));
  }
}
