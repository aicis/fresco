package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.ComparisonBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.eq.Equality;
import dk.alexandra.fresco.lib.compare.gt.GreaterThan;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTest;
import java.math.BigInteger;

public class DefaultComparisonBuilder implements ComparisonBuilder {

  private final BuilderFactoryNumeric factoryNumeric;
  private final ProtocolBuilderNumeric builder;

  public DefaultComparisonBuilder(BuilderFactoryNumeric factoryNumeric,
      ProtocolBuilderNumeric builder) {
    this.factoryNumeric = factoryNumeric;
    this.builder = builder;
  }

  @Override
  public Computation<SInt> compareLong(Computation<SInt> x, Computation<SInt> y) {
    int bitLength = factoryNumeric.getBasicNumericFactory().getMaxBitLength() * 2;
    GreaterThan greaterThanProtocol = new GreaterThan(
        bitLength, BuilderFactoryNumeric.MAGIC_SECURE_NUMBER,
        x, y, factoryNumeric);
    return builder.createSequentialSub(greaterThanProtocol);

  }

  @Override
  public Computation<SInt> equals(Computation<SInt> x, Computation<SInt> y) {
    int maxBitLength = builder.getBasicNumericFactory().getMaxBitLength();
    return equals(maxBitLength, x, y);
  }

  @Override
  public Computation<SInt> equals(int bitLength, Computation<SInt> x, Computation<SInt> y) {
    return builder.createSequentialSub(new Equality(bitLength, x, y));
  }

  @Override
  public Computation<SInt> compare(Computation<SInt> x, Computation<SInt> y) {
    int bitLength = factoryNumeric.getBasicNumericFactory().getMaxBitLength();
    return builder.createSequentialSub(
        new GreaterThan(
            bitLength, BuilderFactoryNumeric.MAGIC_SECURE_NUMBER,
            x, y, factoryNumeric)
    );
  }

  public Computation<SInt> sign(Computation<SInt> x) {
    NumericBuilder input = builder.numeric();
    Computation<SInt> compare =
        compare(input.known(BigInteger.valueOf(0)), x);
    BigInteger oInt = BigInteger.valueOf(2);
    NumericBuilder numericBuilder = builder.numeric();
    Computation<SInt> twice = numericBuilder.mult(oInt, compare);
    return numericBuilder.sub(twice, input.known(BigInteger.valueOf(1)));
  }

  @Override
  public Computation<SInt> compareZero(Computation<SInt> x, int bitLength) {
    return builder.createSequentialSub(
        new ZeroTest(factoryNumeric, bitLength,
            x));
  }

}
