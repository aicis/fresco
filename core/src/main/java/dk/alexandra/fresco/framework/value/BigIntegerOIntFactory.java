package dk.alexandra.fresco.framework.value;

import java.math.BigInteger;

/**
 * A generic {@link OIntFactory} implementation for arithmetic backend suites that use the {@link
 * BigInteger} directly as the underlying open value data type.
 */
public class BigIntegerOIntFactory implements OIntFactory {

  @Override
  public BigInteger toBigInteger(OInt value) {
    return (BigInteger) value;
  }

  @Override
  public OInt fromBigInteger(BigInteger value) {
    return (OInt) value;
  }

}
