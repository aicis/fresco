package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.eq.Equality;
import dk.alexandra.fresco.lib.compare.gt.LessThanOrEquals;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTest;
import java.math.BigInteger;

public class DefaultComparison implements Comparison {

  private final BuilderFactoryNumeric factoryNumeric;
  private final ProtocolBuilderNumeric builder;

  protected DefaultComparison(BuilderFactoryNumeric factoryNumeric,
      ProtocolBuilderNumeric builder) {
    this.factoryNumeric = factoryNumeric;
    this.builder = builder;
  }

  @Override
  public DRes<SInt> compareLEQLong(DRes<SInt> x, DRes<SInt> y) {
    int bitLength = factoryNumeric.getBasicNumericContext().getMaxBitLength() * 2;
    LessThanOrEquals leqProtocol = new LessThanOrEquals(
        bitLength, BuilderFactoryNumeric.MAGIC_SECURE_NUMBER,
        x, y);
    return builder.seq(leqProtocol);

  }

  @Override
  public DRes<SInt> equals(DRes<SInt> x, DRes<SInt> y) {
    int maxBitLength = builder.getBasicNumericContext().getMaxBitLength();
    return equals(maxBitLength, x, y);
  }

  @Override
  public DRes<SInt> equals(int bitLength, DRes<SInt> x, DRes<SInt> y) {
    return builder.seq(new Equality(bitLength, x, y));
  }

  @Override
  public DRes<SInt> compareLEQ(DRes<SInt> x, DRes<SInt> y) {
    int bitLength = factoryNumeric.getBasicNumericContext().getMaxBitLength();
    return builder.seq(
        new LessThanOrEquals(
            bitLength, BuilderFactoryNumeric.MAGIC_SECURE_NUMBER,
            x, y));
  }

  public DRes<SInt> sign(DRes<SInt> x) {
    Numeric input = builder.numeric();
    // TODO create a compareLeqOrEqZero on comparison builder
    DRes<SInt> compare =
        compareLEQ(input.known(BigInteger.valueOf(0)), x);
    BigInteger oInt = BigInteger.valueOf(2);
    Numeric numericBuilder = builder.numeric();
    DRes<SInt> twice = numericBuilder.mult(oInt, compare);
    return numericBuilder.sub(twice, BigInteger.valueOf(1));
  }

  @Override
  public DRes<SInt> compareZero(DRes<SInt> x, int bitLength) {
    return builder.seq(new ZeroTest(bitLength, x));
  }

}
