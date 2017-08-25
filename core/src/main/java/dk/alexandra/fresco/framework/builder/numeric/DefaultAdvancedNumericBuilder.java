package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.conversion.IntegerToBitsByShift;
import dk.alexandra.fresco.lib.math.integer.binary.BitLength;
import dk.alexandra.fresco.lib.math.integer.binary.RepeatedRightShift;
import dk.alexandra.fresco.lib.math.integer.binary.RightShift;
import dk.alexandra.fresco.lib.math.integer.division.KnownDivisor;
import dk.alexandra.fresco.lib.math.integer.division.KnownDivisorRemainder;
import dk.alexandra.fresco.lib.math.integer.division.SecretSharedDivisor;
import dk.alexandra.fresco.lib.math.integer.exp.Exponentiation;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationOpenBase;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationOpenExponent;
import dk.alexandra.fresco.lib.math.integer.inv.Inversion;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProduct;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductOpen;
import dk.alexandra.fresco.lib.math.integer.log.Logarithm;
import dk.alexandra.fresco.lib.math.integer.sqrt.SquareRoot;
import java.math.BigInteger;
import java.util.List;

public class DefaultAdvancedNumericBuilder implements AdvancedNumericBuilder {

  private final BuilderFactoryNumeric factoryNumeric;
  private final ProtocolBuilderNumeric builder;

  protected DefaultAdvancedNumericBuilder(BuilderFactoryNumeric factoryNumeric,
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
    return builder.createSequentialSub(new IntegerToBitsByShift(in, maxInputLength));
  }

  @Override
  public Computation<SInt> exp(Computation<SInt> x, Computation<SInt> e, int maxExponentLength) {
    return builder.createSequentialSub(new Exponentiation(x, e, maxExponentLength));
  }

  @Override
  public Computation<SInt> exp(BigInteger x, Computation<SInt> e, int maxExponentLength) {
    return builder.createSequentialSub(new ExponentiationOpenBase(x, e, maxExponentLength));
  }

  @Override
  public Computation<SInt> exp(Computation<SInt> x, BigInteger e) {
    return builder.createSequentialSub(new ExponentiationOpenExponent(x, e));
  }

  @Override
  public Computation<SInt> sqrt(Computation<SInt> input, int maxInputLength) {
    return builder.createSequentialSub(new SquareRoot(input, maxInputLength));
  }

  @Override
  public Computation<SInt> log(Computation<SInt> input, int maxInputLength) {
    return builder.createSequentialSub(new Logarithm(input, maxInputLength));
  }


  @Override
  public Computation<SInt> dot(List<Computation<SInt>> aVector,
      List<Computation<SInt>> bVector) {
    return builder
        .createSequentialSub(new InnerProduct(aVector, bVector));
  }

  @Override
  public Computation<SInt> openDot(List<BigInteger> aVector, List<Computation<SInt>> bVector) {
    return builder
        .createSequentialSub(new InnerProductOpen(aVector, bVector));
  }

  @Override
  public Computation<RandomAdditiveMask> additiveMask(int noOfBits) {
    return builder
        .createSequentialSub(
            new dk.alexandra.fresco.lib.compare.RandomAdditiveMask(
                BuilderFactoryNumeric.MAGIC_SECURE_NUMBER, noOfBits));
  }

  @Override
  public Computation<SInt> rightShift(Computation<SInt> input) {
    Computation<RightShiftResult> rightShiftResult = builder
        .createSequentialSub(
            new RightShift(
                factoryNumeric.getBasicNumericFactory().getMaxBitLength(),
                input, false));
    return () -> rightShiftResult.out().getResult();
  }

  @Override
  public Computation<RightShiftResult> rightShiftWithRemainder(Computation<SInt> input) {
    return builder.createSequentialSub(
        new RightShift(
            factoryNumeric.getBasicNumericFactory().getMaxBitLength(),
            input, true));
  }

  @Override
  public Computation<SInt> rightShift(Computation<SInt> input, int shifts) {
    Computation<RightShiftResult> rightShiftResult = builder
        .createSequentialSub(
            new RepeatedRightShift(
                input, shifts, false));
    return () -> rightShiftResult.out().getResult();
  }

  @Override
  public Computation<RightShiftResult> rightShiftWithRemainder(
      Computation<SInt> input,
      int shifts) {
    return builder.createSequentialSub(
        new RepeatedRightShift(
            input, shifts, true));
  }

  @Override
  public Computation<SInt> bitLength(Computation<SInt> input, int maxBitLength) {
    return builder.createSequentialSub(
        new BitLength(input, maxBitLength));

  }

  @Override
  public Computation<SInt> invert(Computation<SInt> x) {
    return builder.createSequentialSub(new Inversion(x));
  }
}
