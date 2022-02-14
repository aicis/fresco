package dk.alexandra.fresco.lib.common.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.sort.KeyedCompareAndSwap;
import dk.alexandra.fresco.lib.common.compare.MiscBigIntegerGenerators;
import dk.alexandra.fresco.lib.common.math.integer.conditional.ConditionalSelect;
import dk.alexandra.fresco.lib.common.math.integer.conditional.SwapIf;
import dk.alexandra.fresco.lib.common.math.integer.binary.IntegerToBitsByShift;
import dk.alexandra.fresco.lib.common.math.integer.ProductSIntList;
import dk.alexandra.fresco.lib.common.math.integer.SumSIntList;
import dk.alexandra.fresco.lib.common.math.integer.binary.BitLength;
import dk.alexandra.fresco.lib.common.math.integer.binary.NormalizeSInt;
import dk.alexandra.fresco.lib.common.math.integer.binary.RightShift;
import dk.alexandra.fresco.lib.common.math.integer.binary.Truncate;
import dk.alexandra.fresco.lib.common.math.integer.division.KnownDivisor;
import dk.alexandra.fresco.lib.common.math.integer.division.KnownDivisorRemainder;
import dk.alexandra.fresco.lib.common.math.integer.division.SecretSharedDivisor;
import dk.alexandra.fresco.lib.common.math.integer.exp.Exponentiation;
import dk.alexandra.fresco.lib.common.math.integer.exp.ExponentiationOpenBase;
import dk.alexandra.fresco.lib.common.math.integer.exp.ExponentiationOpenExponent;
import dk.alexandra.fresco.lib.common.math.integer.inv.Inversion;
import dk.alexandra.fresco.lib.common.math.integer.linalg.InnerProduct;
import dk.alexandra.fresco.lib.common.math.integer.linalg.InnerProductOpen;
import dk.alexandra.fresco.lib.common.math.integer.log.Logarithm;
import dk.alexandra.fresco.lib.common.math.integer.mod.Mod2m;
import dk.alexandra.fresco.lib.common.math.integer.sqrt.SquareRoot;
import java.math.BigInteger;
import java.util.List;

/**
 * Default way of producing the protocols within the interface. This default class can be
 * overwritten when implementing {@link BuilderFactoryNumeric} if the protocol suite has a better
 * and more efficient way of constructing the protocols.
 */
public class DefaultAdvancedNumeric implements AdvancedNumeric {

  private final ProtocolBuilderNumeric builder;

  DefaultAdvancedNumeric(ProtocolBuilderNumeric builder) {
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
  public DRes<SInt> div(DRes<SInt> dividend, DRes<SInt> divisor) {
    return builder.seq(new SecretSharedDivisor(dividend, divisor));
  }

  @Override
  public DRes<SInt> mod(DRes<SInt> dividend, BigInteger divisor) {
    return builder.seq(new KnownDivisorRemainder(dividend, divisor));
  }

  @Override
  public DRes<SInt> mod2m(DRes<SInt> dividend, int m) {
    return builder.seq(new Mod2m(dividend, m,
        builder.getBasicNumericContext().getMaxBitLength(),
        builder.getBasicNumericContext().getStatisticalSecurityParam()));
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
  public DRes<SInt> innerProduct(List<DRes<SInt>> vectorA, List<DRes<SInt>> vectorB) {
    return builder.seq(new InnerProduct(vectorA, vectorB));
  }

  @Override
  public DRes<SInt> innerProductWithPublicPart(List<BigInteger> vectorA, List<DRes<SInt>> vectorB) {
    return builder.seq(new InnerProductOpen(vectorA, vectorB));
  }

  @Override
  public DRes<RandomAdditiveMask> additiveMask(int noOfBits) {
    return builder.seq(new dk.alexandra.fresco.lib.common.compare.RandomAdditiveMask(noOfBits));
  }

  @Override
  public DRes<SInt> rightShift(DRes<SInt> input) {
    return builder.seq(new RightShift(
        builder.getBasicNumericContext().getMaxBitLength(),
        input)).seq((seq, result) -> result.result);
  }

  @Override
  public DRes<SInt> rightShift(DRes<SInt> input, int shifts) {
    return builder.seq(new RightShift(
        builder.getBasicNumericContext().getMaxBitLength(),
        input, shifts)).seq((seq, result) -> result.result);
  }

  @Override
  public DRes<RightShiftResult> rightShiftWithRemainder(DRes<SInt> input) {
    return builder.seq(
        new RightShift(
            builder.getBasicNumericContext().getMaxBitLength(),
            input));
  }

  @Override
  public DRes<RightShiftResult> rightShiftWithRemainder(DRes<SInt> input, int shifts) {
    return builder.seq(new RightShift(builder.getBasicNumericContext().getMaxBitLength(),
        input, shifts));
  }

  @Override
  public DRes<SInt> truncate(DRes<SInt> input, int shifts) {
    return builder.seq(new Truncate(input, shifts));
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

  @Override
  public DRes<Pair<DRes<SInt>, DRes<SInt>>> normalize(DRes<SInt> input, int targetBitLength) {
    return builder.seq(new NormalizeSInt(input, targetBitLength));
  }

  @Override
  public DRes<List<Pair<DRes<SInt>, List<DRes<SInt>>>>> keyedCompareAndSwap(
      Pair<DRes<SInt>, List<DRes<SInt>>> leftKeyAndValue,
      Pair<DRes<SInt>, List<DRes<SInt>>> rightKeyAndValue) {
    return builder.seq(KeyedCompareAndSwap.numeric(leftKeyAndValue, rightKeyAndValue));
  }

  @Override
  public DRes<SInt> bitsToInteger(List<DRes<SInt>> bits) {
    MiscBigIntegerGenerators oIntGenerators = new MiscBigIntegerGenerators(
        builder.getBasicNumericContext().getModulus());
    return builder.seq(seq -> AdvancedNumeric.using(seq)
        .innerProductWithPublicPart(oIntGenerators.getTwoPowersList(bits.size()), bits));
  }
}
