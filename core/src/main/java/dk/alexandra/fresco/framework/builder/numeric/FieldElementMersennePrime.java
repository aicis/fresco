package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.util.MathUtils;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.Objects;

public final class FieldElementMersennePrime implements FieldElement {

  private final BigInteger value;
  private final ModulusMersennePrime modulus;

  private FieldElementMersennePrime(BigInteger value, ModulusMersennePrime modulus) {
    if (value.signum() < 0) {
      this.value = value.mod(modulus.getBigInteger());
    } else {
      this.value = modulus.mod(value);
    }
    this.modulus = modulus;
  }

  private FieldElement create(BigInteger newValue) {
    return new FieldElementMersennePrime(newValue, modulus);
  }

  static FieldElement create(BigInteger value, ModulusMersennePrime modulus) {
    return new FieldElementMersennePrime(value, modulus);
  }

  static FieldElement create(int value, ModulusMersennePrime modulus) {
    return create(BigInteger.valueOf(value), modulus);
  }

  static FieldElement create(byte[] bytes, ModulusMersennePrime modulus) {
    return create(new BigInteger(bytes), modulus);
  }

  static FieldElement create(String string, ModulusMersennePrime modulus) {
    return create(new BigInteger(string), modulus);
  }

  @Override
  public FieldElement add(FieldElement operand) {
    return create(value.add(extractValue(operand)));
  }

  @Override
  public FieldElement subtract(FieldElement operand) {
    return create(value.subtract(extractValue(operand)));
  }

  @Override
  public FieldElement negate() {
    return create(value.negate());
  }

  @Override
  public FieldElement multiply(FieldElement operand) {
    return create(value.multiply(extractValue(operand)));
  }

  @Override
  public FieldElement pow(int exponent) {
    return create(value.pow(exponent));
  }

  @Override
  public FieldElement sqrt() {
    return create(MathUtils.modularSqrt(value, getModulus()));
  }

  @Override
  public FieldElement modInverse() {
    return create(value.modInverse(getModulus()));
  }

  @Override
  public boolean getBit(int bitIndex) {
    return value.testBit(bitIndex);
  }

  static BigInteger extractValue(FieldElement element) {
    return ((FieldElementMersennePrime) element).value;
  }

  void toByteArray(byte[] bytes, int offset, int byteLength) {
    byte[] byteArray = toByteArray();
    System.arraycopy(byteArray, 0, bytes, byteLength - byteArray.length + offset, byteArray.length);
  }

  public byte[] toByteArray() {
    return value.toByteArray();
  }

  @Override
  public StrictBitVector toBitVector() {
    return new StrictBitVector(toByteArray());
  }

  private BigInteger getModulus() {
    return modulus.getBigInteger();
  }

  @Override
  public String toString() {
    return "FieldElementMersennePrime{" +
        "value=" + value +
        ", modulus =" + modulus +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FieldElementMersennePrime fieldElementMersennePrime = (FieldElementMersennePrime) o;
    return Objects.equals(modulus, fieldElementMersennePrime.modulus) &&
        Objects.equals(value, fieldElementMersennePrime.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(modulus, value);
  }
}
