package dk.alexandra.fresco.lib.common.compare;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.compare.eq.Equality;
import dk.alexandra.fresco.lib.common.compare.lt.BitLessThanOpen;
import dk.alexandra.fresco.lib.common.compare.lt.Carry;
import dk.alexandra.fresco.lib.common.compare.lt.LessThanOrEquals;
import dk.alexandra.fresco.lib.common.compare.lt.LessThanZero;
import dk.alexandra.fresco.lib.common.compare.min.ArgMin;
import dk.alexandra.fresco.lib.common.compare.zerotest.ZeroTestConstRounds;
import dk.alexandra.fresco.lib.common.compare.zerotest.ZeroTestLogRounds;
import dk.alexandra.fresco.lib.common.util.SIntPair;
import java.math.BigInteger;
import java.util.List;

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
  public DRes<SInt> equals(DRes<SInt> x, DRes<SInt> y, int bitlength, Algorithm algorithm) {
    DRes<SInt> diff = builder.numeric().sub(x, y);
    return compareZero(diff, bitlength, algorithm);
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
  public DRes<SInt> compareLT(DRes<SInt> x, DRes<SInt> y, Algorithm algorithm) {
    if (algorithm == Algorithm.LOG_ROUNDS) {
      if (builder.getBasicNumericContext().getStatisticalSecurityParam() + builder
          .getBasicNumericContext().getMaxBitLength() > builder.getBasicNumericContext()
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
  public DRes<SInt> compareLTBits(BigInteger openValue, DRes<List<DRes<SInt>>> secretBits) {
    return builder.seq(new BitLessThanOpen(openValue, secretBits));
  }

  @Override
  public DRes<List<SIntPair>> carry(List<SIntPair> bitPairs) {
    return builder.seq(new Carry(bitPairs));
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
  public DRes<SInt> compareZero(DRes<SInt> x, int bitlength, Algorithm algorithm) {
    if (bitlength > builder.getBasicNumericContext().getMaxBitLength()) {
      throw new IllegalArgumentException("The bitlength is more than allowed for elements.");
    }
    if (builder.getBasicNumericContext().getStatisticalSecurityParam()
        + bitlength > builder.getBasicNumericContext().getModulus().bitLength()) {
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
    }  }

  @Override
  public DRes<Pair<List<DRes<SInt>>, SInt>> argMin(List<DRes<SInt>> xs) {
    return builder.seq(new ArgMin(xs));
  }

  @Override
  public DRes<SInt> compareZero(DRes<SInt> x, int bitLength) {
    return compareZero(x, bitLength, Algorithm.CONST_ROUNDS);
  }
}
