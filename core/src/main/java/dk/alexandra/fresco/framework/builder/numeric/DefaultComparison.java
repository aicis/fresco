package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.eq.EqualityConstRounds;
import dk.alexandra.fresco.lib.compare.eq.EqualityLogRounds;
import dk.alexandra.fresco.lib.compare.lt.BitLessThanOpen;
import dk.alexandra.fresco.lib.compare.lt.LessThanLogRounds;
import dk.alexandra.fresco.lib.compare.lt.LessThanOrEquals;
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

  // Security parameter used by protocols using rightshifts and/or additive masks.
  protected final int magicSecureNumber = 40;
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
    LessThanOrEquals leqProtocol = new LessThanOrEquals(
        bitLength, magicSecureNumber, x, y);
    return builder.seq(leqProtocol);

  }

  @Override
  public DRes<SInt> equals(DRes<SInt> x, DRes<SInt> y,
      EqualityAlgorithm algorithm) {
    int maxBitLength = 64;// builder.getBasicNumericContext().getMaxBitLength();
    switch (algorithm) {
    case EQ_CONST_ROUNDS:
      return equalsConstRounds(maxBitLength, x, y);
    case EQ_LOG_ROUNDS:
      return equals(maxBitLength, x, y);
    default:
      throw new UnsupportedOperationException("Not implemented yet");
    }
  }

  @Override
  public DRes<SInt> equals(int bitLength, DRes<SInt> x, DRes<SInt> y) {
    // TODO throw if maxBitLength + securityParameter > mod bit length
    return builder.seq(new EqualityLogRounds(bitLength, x, y));
  }

  public DRes<SInt> equalsConstRounds(int bitLength, DRes<SInt> x,
      DRes<SInt> y) {
    return builder.seq(new EqualityConstRounds(bitLength, x, y));
  }

  @Override
  public DRes<SInt> compareLEQ(DRes<SInt> x, DRes<SInt> y) {
    int bitLength = factoryNumeric.getBasicNumericContext().getMaxBitLength();
    return builder.seq(
        new LessThanOrEquals(bitLength, magicSecureNumber, x, y));
  }

  @Override
  public DRes<SInt> compareLT(DRes<SInt> x1, DRes<SInt> x2, ComparisonAlgorithm algorithm) {
    if (algorithm == ComparisonAlgorithm.LT_LOG_ROUNDS) {
      int k = builder.getBasicNumericContext().getMaxBitLength();
      // TODO throw if k + kappa > mod bit length
      return builder.seq(new LessThanLogRounds(x1, x2, k, magicSecureNumber));
    } else {
      throw new UnsupportedOperationException("Not implemented yet");
    }
  }

  @Override
  public DRes<SInt> compareLTBits(DRes<OInt> openValue, DRes<List<DRes<SInt>>> secretBits) {
    return builder.seq(new BitLessThanOpen(openValue, secretBits));
  }

  @Override
  public DRes<SInt> compareLTBits(OInt openValue, List<DRes<SInt>> secretBits) {
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
  public DRes<SInt> compareZero(DRes<SInt> x, int bitLength) {
    return builder.seq(new ZeroTestLogRounds(bitLength, x, magicSecureNumber));
  }

  public DRes<SInt> compareZeroConstRounds(DRes<SInt> x, int bitLength) {
    return builder.seq(new ZeroTestConstRounds(bitLength, x, magicSecureNumber));
  }

  @Override
  public DRes<SInt> compareZero(DRes<SInt> x, int bitLength,
      EqualityAlgorithm algorithm) {
    // int maxBitLength = builder.getBasicNumericContext().getMaxBitLength();
    switch (algorithm) {
    case EQ_CONST_ROUNDS:
      return compareZeroConstRounds(x, bitLength);
    case EQ_LOG_ROUNDS:
      return compareZero(x, bitLength);
    default:
      throw new UnsupportedOperationException("Not implemented yet");
    }
  }
}
