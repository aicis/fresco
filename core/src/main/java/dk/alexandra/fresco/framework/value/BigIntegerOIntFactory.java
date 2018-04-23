package dk.alexandra.fresco.framework.value;

import java.math.BigInteger;

/**
 * A generic {@link OIntFactory} implementation for arithmetic backend suites that use the {@link
 * BigInteger} directly as the underlying open value data type.
 */
public class BigIntegerOIntFactory implements OIntFactory {

  private static final OInt ZERO = new OIntBigInteger(BigInteger.ZERO);
  private static final OInt ONE = new OIntBigInteger(BigInteger.ONE);
  private static final OInt TWO = new OIntBigInteger(BigInteger.valueOf(2));

  @Override
  public BigInteger toBigInteger(OInt value) {
    return ((OIntBigInteger) value).getValue();
  }

  @Override
  public OInt fromBigInteger(BigInteger value) {
    return new OIntBigInteger(value);
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

}
