package dk.alexandra.fresco.framework.value;

import java.math.BigInteger;

/**
 * A generic {@link OIntFactory} implementation for arithmetic backend suites that use the {@link
 * BigInteger} directly as the underlying open value data type.
 */
public class BigIntegerOIntFactory implements OIntFactory {

  private static final OInt ZERO = new BigIntegerOInt(BigInteger.ZERO);
  private static final OInt ONE = new BigIntegerOInt(BigInteger.ONE);
  private static final OInt TWO = new BigIntegerOInt(BigInteger.valueOf(2));
  private final int maxBitLength;

  public BigIntegerOIntFactory(int maxBitLength) {
    this.maxBitLength = maxBitLength;
  }

  @Override
  public BigInteger toBigInteger(OInt value) {
    return ((BigIntegerOInt) value).getValue();
  }

  @Override
  public OInt fromBigInteger(BigInteger value) {
    return new BigIntegerOInt(value);
  }

  @Override
  public OInt zero() {
    return ZERO;
  }

  @Override
  public OInt one() {
    return ONE;
  }

  @Override
  public OInt two() {
    return TWO;
  }

  @Override
  public int getMaxBitLength() {
    return maxBitLength;
  }

}
