package dk.alexandra.fresco.lib.common.compare;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.compare.eq.Equality;
import dk.alexandra.fresco.lib.common.compare.gt.LessThanOrEquals;
import dk.alexandra.fresco.lib.common.compare.zerotest.ZeroTest;
import java.math.BigInteger;

/**
 * Default way of producing the protocols within the interface. This default class can be
 * overwritten when implementing {@link BuilderFactoryNumeric} if the protocol suite has a better
 * and more efficient way of constructing the protocols.
 */
public class DefaultComparison implements Comparison {

  // Security parameter used by protocols using rightshifts and/or additive masks.
  private final int magicSecureNumber = 60;
  private final int maxBitLength;
  private final ProtocolBuilderNumeric builder;

  DefaultComparison(ProtocolBuilderNumeric builder) {
    this.maxBitLength = builder.getBasicNumericContext().getMaxBitLength();
    this.builder = builder;
  }

  @Override
  public DRes<SInt> compareLEQLong(DRes<SInt> x, DRes<SInt> y) {
    int bitLength = maxBitLength * 2;
    LessThanOrEquals leqProtocol = new LessThanOrEquals(bitLength, magicSecureNumber, x, y);
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
    return builder.seq(new LessThanOrEquals(maxBitLength, magicSecureNumber, x, y));
  }

  @Override
  public DRes<SInt> sign(DRes<SInt> x) {
    Numeric input = builder.numeric();
    // TODO create a compareLeqOrEqZero on comparison builder
    DRes<SInt> compare = compareLEQ(input.known(BigInteger.ZERO), x);
    BigInteger oint = BigInteger.valueOf(2);
    Numeric numericBuilder = builder.numeric();
    DRes<SInt> twice = numericBuilder.mult(oint, compare);
    return numericBuilder.sub(twice, BigInteger.valueOf(1));
  }

  @Override
  public DRes<SInt> compareZero(DRes<SInt> x, int bitLength) {
    return builder.seq(new ZeroTest(bitLength, x, magicSecureNumber));
  }
}
