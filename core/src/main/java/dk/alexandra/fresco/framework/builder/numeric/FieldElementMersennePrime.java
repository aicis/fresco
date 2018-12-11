package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import java.util.Objects;

public final class FieldElementMersennePrime implements FieldElement {

  private final BigInteger value;
  private final ModulusMersennePrime modulus;

  private FieldElementMersennePrime(BigInteger value, ModulusMersennePrime modulus) {
    this.value = modulus.mod(value);
    this.modulus = modulus;
  }

  public static FieldElementMersennePrime create(int value, ModulusMersennePrime modulus) {
    return new FieldElementMersennePrime(BigInteger.valueOf(value), modulus);
  }

  public static FieldElementMersennePrime create(byte[] bytes, ModulusMersennePrime modulus) {
    return new FieldElementMersennePrime(new BigInteger(bytes), modulus);
  }

  public static FieldElementMersennePrime create(String asString, ModulusMersennePrime modulus) {
    return new FieldElementMersennePrime(new BigInteger(asString), modulus);
  }

  public static FieldElementMersennePrime create(BigInteger value, ModulusMersennePrime modulus) {
    return new FieldElementMersennePrime(value, modulus);
  }

  private FieldElementMersennePrime create(BigInteger newValue) {
    return new FieldElementMersennePrime(newValue, modulus);
  }

  @Override
  public FieldElementMersennePrime subtract(FieldElement operand) {
    return create(value.subtract(getValue(operand)));
  }

  @Override
  public FieldElementMersennePrime multiply(FieldElement operand) {
    return create(value.multiply(getValue(operand)));
  }

  @Override
  public FieldElementMersennePrime add(FieldElement operand) {
    return create(value.add(getValue(operand)));
  }

  private BigInteger getValue(FieldElement operand) {
    return ((FieldElementMersennePrime) operand).value;
  }

  @Override
  public BigInteger convertToBigInteger() {
    return value;
  }

  @Override
  public void toByteArray(byte[] bytes, int offset, int byteLength) {
    byte[] byteArray = value.toByteArray();
    System.arraycopy(byteArray, 0, bytes, byteLength - byteArray.length + offset, byteArray.length);
  }

  @Override
  public int compareTo(FieldElement o) {
    return value.compareTo(o.convertToBigInteger());
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
