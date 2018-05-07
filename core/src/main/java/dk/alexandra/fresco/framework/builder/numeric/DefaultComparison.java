package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.lt.BitLessThanOpen;
import dk.alexandra.fresco.lib.compare.lt.LessThanOrEquals;
import dk.alexandra.fresco.lib.compare.lt.LessThanZero;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTestConstRounds;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTestLogRounds;

import java.math.BigInteger;
import java.util.List;

/**
 * Default way of producing the protocols within the interface. This default class can be
 * overwritten when implementing {@link BuilderFactoryNumeric} if the protocol suite has a better
 * and more efficient way of constructing the protocols.
 */
public class DefaultComparison implements Comparison {

  protected final BuilderFactoryNumeric factoryNumeric;
  protected final ProtocolBuilderNumeric builder;

  public DefaultComparison(BuilderFactoryNumeric factoryNumeric,
      ProtocolBuilderNumeric builder) {
    this.factoryNumeric = factoryNumeric;
    this.builder = builder;
  }

  @Override
  public DRes<SInt> compareLEQLong(DRes<SInt> x, DRes<SInt> y) {
    int bitLength = factoryNumeric.getBasicNumericContext().getMaxBitLength() * 2;
    LessThanOrEquals leqProtocol = new LessThanOrEquals(bitLength, x, y);
    return builder.seq(leqProtocol);
  }

  @Override
  public DRes<SInt> compareLEQ(DRes<SInt> x, DRes<SInt> y) {
    int bitLength = factoryNumeric.getBasicNumericContext().getMaxBitLength();
    return builder.seq(new LessThanOrEquals(bitLength, x, y));
  }

  @Override
  public DRes<SInt> compareLT(DRes<SInt> x, DRes<SInt> y, Algorithm algorithm) {
    if (algorithm == Algorithm.LOG_ROUNDS) {
      if (factoryNumeric.getBasicNumericContext().getStatisticalSecurityParam() + factoryNumeric
          .getBasicNumericContext().getMaxBitLength() > factoryNumeric.getBasicNumericContext()
          .getModulus().bitLength()) {
        throw new IllegalArgumentException(
            "The max bitlength plus the statistical security parameter overflows the size of the modulus.");
      }
      DRes<SInt> difference = builder.numeric().sub(x, y);
      return builder.seq(new LessThanZero(difference));
    } else {
      throw new UnsupportedOperationException("Not implemented yet");
    }
  }

  @Override
  public DRes<SInt> compareLTBits(DRes<OInt> openValue, DRes<List<DRes<SInt>>> secretBits) {
    return builder.seq(new BitLessThanOpen(openValue, secretBits));
  }

  @Override
  public DRes<SInt> sign(DRes<SInt> x) {
    Numeric input = builder.numeric();
    // TODO create a compareLeqOrEqZero on comparison builder
    DRes<SInt> compare =
        compareLEQ(input.known(BigInteger.ZERO), x);
    BigInteger oint = BigInteger.valueOf(2);
    Numeric numericBuilder = builder.numeric();
    DRes<SInt> twice = numericBuilder.mult(oint, compare);
    return numericBuilder.sub(twice, BigInteger.valueOf(1));
  }

  @Override
  public DRes<SInt> equals(DRes<SInt> x, DRes<SInt> y, int bitlength, Algorithm algorithm) {
    DRes<SInt> diff = builder.numeric().sub(x, y);
    return compareZero(diff, bitlength, algorithm);
  }

  @Override
  public DRes<SInt> equals(DRes<SInt> x, DRes<SInt> y) {
    int bitLength = factoryNumeric.getBasicNumericContext().getMaxBitLength();
    return equals(x, y, bitLength);
  }

  @Override
  public DRes<SInt> compareZero(DRes<SInt> x, int bitlength, Algorithm algorithm) {
    if (bitlength > factoryNumeric.getBasicNumericContext().getMaxBitLength()) {
      throw new IllegalArgumentException("The bitlength is more than allowed for elements.");
    }
    if (factoryNumeric.getBasicNumericContext().getStatisticalSecurityParam()
        + bitlength > factoryNumeric.getBasicNumericContext().getModulus().bitLength()) {
      throw new IllegalArgumentException(
          "The max bitlength plus the statistical security parameter overflows the size of the modulus.");
    }
    switch (algorithm) {
      case CONST_ROUNDS:
        return builder.seq(new ZeroTestConstRounds(x, bitlength));
      case LOG_ROUNDS:
        return builder.seq(new ZeroTestLogRounds(x, bitlength));
      default:
        throw new UnsupportedOperationException("Not implemented yet");
    }
  }

}
