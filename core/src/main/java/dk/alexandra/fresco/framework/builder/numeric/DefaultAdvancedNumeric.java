package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.conditional.ConditionalSelect;
import dk.alexandra.fresco.lib.conditional.SwapIf;
import dk.alexandra.fresco.lib.conversion.IntegerToBitsByShift;
import dk.alexandra.fresco.lib.math.integer.ProductSIntList;
import dk.alexandra.fresco.lib.math.integer.SumSIntList;
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

/**
 * Default way of producing the protocols within the interface. This default class can be
 * overwritten when implementing {@link BuilderFactoryNumeric} if the protocol suite has a better
 * and more efficient way of constructing the protocols.
 */
public class DefaultAdvancedNumeric implements AdvancedNumeric {

  private final BuilderFactoryNumeric factoryNumeric;
  private final ProtocolBuilderNumeric builder;

  protected DefaultAdvancedNumeric(BuilderFactoryNumeric factoryNumeric,
      ProtocolBuilderNumeric builder) {
    this.factoryNumeric = factoryNumeric;
    this.builder = builder;
  }

  @Override
  public DRes<SInt> sum(List<DRes<SInt>> inputs) {
    return builder.seq(new SumSIntList(inputs));
  }

  @Override
  public DRes<SInt> product(List<DRes<SInt>> elements) {
    return builder.seq(new ProductSIntList(elements));
  }

  @Override
  public DRes<SInt> div(DRes<SInt> dividend, BigInteger divisor) {
    return builder.seq(new KnownDivisor(dividend, divisor));
  }

  @Override
  public DRes<SInt> mod(DRes<SInt> dividend, BigInteger divisor) {
    return builder.seq(new KnownDivisorRemainder(dividend, divisor));
  }

  @Override
  public DRes<SInt> div(DRes<SInt> dividend, DRes<SInt> divisor) {
    return builder.seq(new SecretSharedDivisor(dividend, divisor));
  }

  @Override
  public DRes<List<SInt>> toBits(DRes<SInt> in, int maxInputLength) {
    return builder.seq(new IntegerToBitsByShift(in, maxInputLength));
  }

  @Override
  public DRes<SInt> exp(DRes<SInt> x, DRes<SInt> e, int maxExponentLength) {
    return builder.seq(new Exponentiation(x, e, maxExponentLength));
  }

  @Override
  public DRes<SInt> exp(BigInteger x, DRes<SInt> e, int maxExponentLength) {
    return builder.seq(new ExponentiationOpenBase(x, e, maxExponentLength));
  }

  @Override
  public DRes<SInt> exp(DRes<SInt> x, BigInteger e) {
    return builder.seq(new ExponentiationOpenExponent(x, e));
  }

  @Override
  public DRes<SInt> sqrt(DRes<SInt> input, int maxInputLength) {
    return builder.seq(new SquareRoot(input, maxInputLength));
  }

  @Override
  public DRes<SInt> log(DRes<SInt> input, int maxInputLength) {
    return builder.seq(new Logarithm(input, maxInputLength));
  }


  @Override
  public DRes<SInt> innerProduct(List<DRes<SInt>> aVector,
      List<DRes<SInt>> bVector) {
    return builder.seq(new InnerProduct(aVector, bVector));
  }

  @Override
  public DRes<SInt> innerProductWithPublicPart(List<BigInteger> aVector, List<DRes<SInt>> bVector) {
    return builder.seq(new InnerProductOpen(aVector, bVector));
  }

  @Override
  public DRes<RandomAdditiveMask> additiveMask(int noOfBits) {
    return builder.seq(new dk.alexandra.fresco.lib.compare.RandomAdditiveMask(
        BuilderFactoryNumeric.MAGIC_SECURE_NUMBER, noOfBits));
  }

  @Override
  public DRes<SInt> rightShift(DRes<SInt> input) {
    DRes<RightShiftResult> rightShiftResult = builder.seq(
        new RightShift(
            factoryNumeric.getBasicNumericContext().getMaxBitLength(),
            input, false));
    return () -> rightShiftResult.out().getResult();
  }

  @Override
  public DRes<RightShiftResult> rightShiftWithRemainder(DRes<SInt> input) {
    return builder.seq(
        new RightShift(
            factoryNumeric.getBasicNumericContext().getMaxBitLength(),
            input, true));
  }

  @Override
  public DRes<SInt> rightShift(DRes<SInt> input, int shifts) {
    DRes<RightShiftResult> rightShiftResult = builder.seq(
        new RepeatedRightShift(
            input, shifts, false));
    return () -> rightShiftResult.out().getResult();
  }

  @Override
  public DRes<RightShiftResult> rightShiftWithRemainder(
      DRes<SInt> input,
      int shifts) {
    return builder.seq(
        new RepeatedRightShift(
            input, shifts, true));
  }

  @Override
  public DRes<SInt> bitLength(DRes<SInt> input, int maxBitLength) {
    return builder.seq(new BitLength(input, maxBitLength));

  }

  @Override
  public DRes<SInt> invert(DRes<SInt> x) {
    return builder.seq(new Inversion(x));
  }

  @Override
  public DRes<SInt> condSelect(DRes<SInt> condition, DRes<SInt> left, DRes<SInt> right) {
    return builder.seq(new ConditionalSelect(condition, left, right));
  }

  @Override
  public DRes<Pair<DRes<SInt>, DRes<SInt>>> swapIf(DRes<SInt> condition, DRes<SInt> left,
      DRes<SInt> right) {
    return builder.par(new SwapIf(condition, left, right));
  }
}
