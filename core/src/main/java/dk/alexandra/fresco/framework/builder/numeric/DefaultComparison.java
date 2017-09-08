package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.eq.Equality;
import dk.alexandra.fresco.lib.compare.gt.LessThanOrEquals;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTest;
import java.math.BigInteger;

public class DefaultComparisonBuilder implements ComparisonBuilder {

  private final BuilderFactoryNumeric factoryNumeric;
  private final ProtocolBuilderNumeric builder;

  protected DefaultComparisonBuilder(BuilderFactoryNumeric factoryNumeric,
      ProtocolBuilderNumeric builder) {
    this.factoryNumeric = factoryNumeric;
    this.builder = builder;
  }

  @Override
  public Computation<SInt> compareLEQLong(Computation<SInt> x, Computation<SInt> y) {
    int bitLength = factoryNumeric.getBasicNumericFactory().getMaxBitLength() * 2;
    LessThanOrEquals leqProtocol = new LessThanOrEquals(
        bitLength, BuilderFactoryNumeric.MAGIC_SECURE_NUMBER,
        x, y);
    return builder.seq(leqProtocol);

  }

  @Override
  public Computation<SInt> equals(Computation<SInt> x, Computation<SInt> y) {
    int maxBitLength = builder.getBasicNumericContext().getMaxBitLength();
    return equals(maxBitLength, x, y);
  }

  @Override
  public Computation<SInt> equals(int bitLength, Computation<SInt> x, Computation<SInt> y) {
    return builder.seq(new Equality(bitLength, x, y));
  }

  @Override
  public Computation<SInt> compareLEQ(Computation<SInt> x, Computation<SInt> y) {
    int bitLength = factoryNumeric.getBasicNumericFactory().getMaxBitLength();
    return builder.seq(
        new LessThanOrEquals(
            bitLength, BuilderFactoryNumeric.MAGIC_SECURE_NUMBER,
            x, y));
  }

  public Computation<SInt> sign(Computation<SInt> x) {
    NumericBuilder input = builder.numeric();
    // TODO create a compareLeqOrEqZero on comparison builder
    Computation<SInt> compare =
        compareLEQ(input.known(BigInteger.valueOf(0)), x);
    BigInteger oInt = BigInteger.valueOf(2);
    NumericBuilder numericBuilder = builder.numeric();
    Computation<SInt> twice = numericBuilder.mult(oInt, compare);
    return numericBuilder.sub(twice, BigInteger.valueOf(1));
  }

  @Override
  public Computation<SInt> compareZero(Computation<SInt> x, int bitLength) {
    return builder.seq(new ZeroTest(bitLength, x));
  }

}
