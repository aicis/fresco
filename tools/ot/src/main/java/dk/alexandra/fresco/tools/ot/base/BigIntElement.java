package dk.alexandra.fresco.tools.ot.base;

import java.math.BigInteger;

public class BigIntElement implements InterfaceNaorPinkasElement<BigIntElement> {

  private final BigInteger element;
  private final BigInteger dhModulus;

  public BigIntElement(BigInteger element, BigInteger dhModulus) {
    this.element = element;
    this.dhModulus = dhModulus;
  }

  @Override
  public byte[] toByteArray() {
    return this.element.toByteArray();
  }

  @Override
  public BigIntElement groupOp(BigIntElement other) {
    return new BigIntElement(this.element.multiply(other.element), this.dhModulus);
  }

  @Override
  public BigIntElement inverse() {
    return new BigIntElement(this.element.modInverse(this.dhModulus), this.dhModulus);
  }

  @Override
  //modPow in this case
  public BigIntElement exponentiation(BigInteger n) {
    return new BigIntElement(this.element.modPow(n, this.dhModulus), this.dhModulus);
  }
}
