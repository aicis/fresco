package dk.alexandra.fresco.lib.math.integer.division;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.AdvancedNumericBuilder;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskProtocol44;
import dk.alexandra.fresco.lib.conversion.IntegerToBitsByShiftProtocolImpl4;
import dk.alexandra.fresco.lib.math.integer.binary.BitLengthProtocol4;
import dk.alexandra.fresco.lib.math.integer.binary.RepeatedRightShiftProtocol4;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftProtocol4;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationProtocol4;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationProtocolOpenBase;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationProtocolOpenExponent;
import dk.alexandra.fresco.lib.math.integer.inv.Inversion;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductProtocol44;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductProtocolOpen;
import dk.alexandra.fresco.lib.math.integer.log.LogarithmProtocol;
import dk.alexandra.fresco.lib.math.integer.sqrt.SquareRootProtocol;
import java.math.BigInteger;
import java.util.List;

public class DefaultAdvancedNumericBuilder implements
    AdvancedNumericBuilder {

  private final BuilderFactoryNumeric factoryNumeric;
  private final ProtocolBuilderNumeric builder;

  public DefaultAdvancedNumericBuilder(BuilderFactoryNumeric factoryNumeric,
      ProtocolBuilderNumeric builder) {
    this.factoryNumeric = factoryNumeric;
    this.builder = builder;
  }


  @Override
  public Computation<SInt> div(Computation<SInt> dividend, BigInteger divisor) {
    return builder
        .createSequentialSub(new KnownDivisor(factoryNumeric, dividend, divisor));
  }

  @Override
  public Computation<SInt> mod(Computation<SInt> dividend, BigInteger divisor) {
    return builder.createSequentialSub(new KnownDivisorRemainder(dividend, divisor));
  }

  @Override
  public Computation<SInt> div(Computation<SInt> dividend, Computation<SInt> divisor) {
    return builder.createSequentialSub(
        new SecretSharedDivisor(dividend, divisor, factoryNumeric)
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
  public Computation<SInt> exp(BigInteger x, Computation<SInt> e, int maxExponentLength) {
    return builder.createSequentialSub(new ExponentiationProtocolOpenBase(x, e, maxExponentLength));
  }

  @Override
  public Computation<SInt> exp(Computation<SInt> x, BigInteger e) {
    return builder.createSequentialSub(new ExponentiationProtocolOpenExponent(x, e));
  }

  @Override
  public Computation<SInt> sqrt(Computation<SInt> input, int maxInputLength) {
    return builder.createSequentialSub(new SquareRootProtocol(input, maxInputLength));
  }

  @Override
  public Computation<SInt> log(Computation<SInt> input, int maxInputLength) {
    return builder.createSequentialSub(new LogarithmProtocol(input, maxInputLength));
  }


  @Override
  public Computation<SInt> dot(List<Computation<SInt>> aVector,
      List<Computation<SInt>> bVector) {
    return builder
        .createSequentialSub(new InnerProductProtocol44(aVector, bVector));
  }

  @Override
  public Computation<SInt> openDot(List<BigInteger> aVector, List<Computation<SInt>> bVector) {
    return builder
        .createSequentialSub(new InnerProductProtocolOpen(aVector, bVector));
  }

  @Override
  public Computation<RandomAdditiveMask> additiveMask(int noOfBits) {
    return builder
        .createSequentialSub(
            new RandomAdditiveMaskProtocol44(factoryNumeric,
                BuilderFactoryNumeric.MAGIC_SECURE_NUMBER, noOfBits));
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

  @Override
  public Computation<SInt> bitLength(Computation<SInt> input, int maxBitLength) {
    return builder.createSequentialSub(
        new BitLengthProtocol4(input, maxBitLength));

  }

  @Override
  public Computation<SInt> invert(Computation<SInt> x) {
    return builder.createSequentialSub(new Inversion(x));
  }
}
