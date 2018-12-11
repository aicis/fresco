package dk.alexandra.fresco.framework.builder.numeric;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

public final class ModulusMersennePrime implements Serializable {

  private final MersennePrime mersenne;
  private final BigInteger halved;

  public ModulusMersennePrime(int bitLength, int constant) {
    this.mersenne = new MersennePrime(bitLength, constant);
    this.halved = this.mersenne.getPrime().divide(BigInteger.valueOf(2));
  }

  public BigInteger getBigInteger() {
    return mersenne.getPrime();
  }

  public BigInteger getBigIntegerHalved() {
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
    return Objects.equals(mersenne, that.mersenne);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mersenne);
  }

  @Override
  public String toString() {
    return "ModulusMersennePrime{" +
        "value=" + mersenne.getPrime() +
        '}';
  }

  BigInteger mod(BigInteger value) {
    int comparison = value.compareTo(mersenne.getPrime());
    if (comparison < 0) {
      return value;
    } else if (comparison == 0) {
      return BigInteger.ZERO;
    }
    BigInteger quotient = value.shiftRight(mersenne.getBitLength());
    // q = z / b^n
    // r = z mod b^n
    BigInteger result = value.and(mersenne.getModulo());
    while (quotient.compareTo(BigInteger.ZERO) > 0) {
      BigInteger product = quotient.multiply(mersenne.getConstant());
      //r = r + (c * q mod b^n)
      result = result.add(product.and(mersenne.getModulo()));

      //q = c*q / b^n
      quotient = product.shiftRight(mersenne.getBitLength());
    }

    while (result.compareTo(mersenne.getPrime()) >= 0) {
      result = result.subtract(mersenne.getPrime());
    }
    return result;
  }

  private static final class MersennePrime {

    private final int bitLength;
    private final BigInteger constant;
    private final BigInteger modulo;
    private final BigInteger prime;

    private MersennePrime(int bitLength, int constant) {
      this.bitLength = bitLength;
      this.constant = BigInteger.valueOf(constant);
      BigInteger shifted = BigInteger.ONE.shiftLeft(bitLength);
      this.modulo = shifted.subtract(BigInteger.ONE);
      this.prime = shifted.subtract(BigInteger.valueOf(constant));
    }

    private int getBitLength() {
      return bitLength;
    }

    private BigInteger getConstant() {
      return constant;
    }

    private BigInteger getModulo() {
      return modulo;
    }

    private BigInteger getPrime() {
      return prime;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      MersennePrime that = (MersennePrime) o;
      return bitLength == that.bitLength && constant.equals(that.constant);
    }

    @Override
    public int hashCode() {
      return Objects.hash(bitLength, constant);
    }
  }
}
