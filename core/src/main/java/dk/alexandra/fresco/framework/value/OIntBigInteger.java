package dk.alexandra.fresco.framework.value;

import java.math.BigInteger;

/**
 * An open value wrapper for {@link BigInteger}.
 */
public class OIntBigInteger implements OInt {

  private final BigInteger value;

  public OIntBigInteger(BigInteger value) {
    this.value = value;
  }

  public BigInteger getValue() {
    return value;
  }

  @Override
  public OInt out() {
    return this;
  }

}
