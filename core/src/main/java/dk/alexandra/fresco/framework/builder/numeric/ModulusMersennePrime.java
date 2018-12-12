package dk.alexandra.fresco.framework.builder.numeric;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

public final class ModulusMersennePrime implements Serializable {

  private final int bitLength;
  private final BigInteger constant;
  private final BigInteger precomputedBitMask;
  private final BigInteger prime;
  private final BigInteger halved;

  public ModulusMersennePrime(int bitLength, int constant) {
    this.bitLength = bitLength;
    this.constant = BigInteger.valueOf(constant);
    BigInteger shifted = BigInteger.ONE.shiftLeft(bitLength);
    this.precomputedBitMask = shifted.subtract(BigInteger.ONE);
    this.prime = shifted.subtract(BigInteger.valueOf(constant));
    this.halved = this.prime.divide(BigInteger.valueOf(2));
  }

  BigInteger getBigInteger() {
    return prime;
  }

  BigInteger getBigIntegerHalved() {
    return halved;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ModulusMersennePrime that = (ModulusMersennePrime) o;
    return bitLength == that.bitLength && constant.equals(that.constant);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bitLength, constant);
  }

  @Override
  public String toString() {
    return "ModulusMersennePrime{" +
        "value=" + prime +
        '}';
  }

  BigInteger mod(BigInteger value) {
    int comparison = value.compareTo(prime);
    if (comparison < 0) {
      return value;
    } else if (comparison == 0) {
      return BigInteger.ZERO;
    }
    BigInteger quotient = value.shiftRight(bitLength);
    // q = z / b^n
    // r = z mod b^n
    BigInteger result = value.and(precomputedBitMask);
    while (quotient.signum() > 0) {
      BigInteger product = quotient.multiply(constant);
      //r = r + (c * q mod b^n)
      result = result.add(product.and(precomputedBitMask));

      //q = c*q / b^n
      quotient = product.shiftRight(bitLength);
    }

    while (result.compareTo(prime) >= 0) {
      result = result.subtract(prime);
    }
    return result;
  }
}
