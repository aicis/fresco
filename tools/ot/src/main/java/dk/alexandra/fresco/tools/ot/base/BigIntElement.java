package dk.alexandra.fresco.tools.ot.base;

import java.math.BigInteger;

public class BigIntElement implements InterfaceNaorPinkasElement {

  BigInteger element;
  BigInteger dhModulus;

  public BigIntElement(BigInteger element, BigInteger dhModulus) {
    this.element = element;
    this.dhModulus = dhModulus;
  }

  @Override
  public byte[] toByteArray() {
    return this.element.toByteArray();
  }

  @Override
  public InterfaceNaorPinkasElement groupOp(InterfaceNaorPinkasElement other) {
    BigInteger otherBigInt = ((BigIntElement) other).element;
    return new BigIntElement(this.element.multiply(otherBigInt), this.dhModulus);
  }

  @Override
  public InterfaceNaorPinkasElement inverse() {
    return new BigIntElement(this.element.modInverse(this.dhModulus), this.dhModulus);
  }

  @Override
  //modPow in this case
  public InterfaceNaorPinkasElement exponentiation(BigInteger n) {
    return new BigIntElement(this.element.modPow(other, this.dhModulus), this.dhModulus);
  }
}
