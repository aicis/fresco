package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.RightShiftBuilder;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;

public class DefaultRightShiftBuilder implements RightShiftBuilder {

  private final BuilderFactoryNumeric factoryNumeric;
  private final ProtocolBuilder builder;

  public DefaultRightShiftBuilder(
      BuilderFactoryNumeric factoryNumeric,
      ProtocolBuilder builder) {
    this.factoryNumeric = factoryNumeric;
    this.builder = builder;
  }

  @Override
  public Computation<SInt> rightShift(Computation<SInt> input) {
    Computation<RightShiftResult> rightShiftResult = builder
        .createSequentialSub(
            new RightShiftProtocol4(
                factoryNumeric.getBasicNumericFactory().getMaxBitLength(),
                input, false));
    return () -> rightShiftResult.out().getResult();
  }

  @Override
  public Computation<RightShiftResult> rightShiftWithRemainder(Computation<SInt> input) {
    return builder.createSequentialSub(
        new RightShiftProtocol4(
            factoryNumeric.getBasicNumericFactory().getMaxBitLength(),
            input, true));
  }

  @Override
  public Computation<SInt> rightShift(Computation<SInt> input, int shifts) {
    Computation<RightShiftResult> rightShiftResult = builder
        .createSequentialSub(
            new RepeatedRightShiftProtocol4(
                input, shifts, false));
    return () -> rightShiftResult.out().getResult();
  }

  @Override
  public Computation<RightShiftResult> rightShiftWithRemainder(
      Computation<SInt> input,
      int shifts) {
    return builder.createSequentialSub(
        new RepeatedRightShiftProtocol4(
            input, shifts, true));
  }
}
