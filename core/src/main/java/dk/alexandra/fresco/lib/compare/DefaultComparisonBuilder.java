package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.ComparisonBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.eq.EqualityProtocol4;
import dk.alexandra.fresco.lib.compare.gt.GreaterThanReducerProtocol4;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTestProtocol4;
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
    GreaterThanReducerProtocol4 greaterThanProtocol = new GreaterThanReducerProtocol4(
        bitLength, BuilderFactoryNumeric.MAGIC_SECURE_NUMBER,
        x, y, factoryNumeric);
    return builder.createSequentialSub(greaterThanProtocol);

  }

  @Override
  public Computation<SInt> equals(Computation<SInt> x, Computation<SInt> y) {
    int maxBitLength = builder.getBasicNumericFactory().getMaxBitLength();
    return builder.createSequentialSub(new EqualityProtocol4(maxBitLength, x, y));
  }

  @Override
  public Computation<SInt> compare(Computation<SInt> x, Computation<SInt> y) {
    int bitLength = factoryNumeric.getBasicNumericFactory().getMaxBitLength();
    return builder.createSequentialSub(
        new GreaterThanReducerProtocol4(
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
        new ZeroTestProtocol4(factoryNumeric, bitLength,
            x));
  }

}
