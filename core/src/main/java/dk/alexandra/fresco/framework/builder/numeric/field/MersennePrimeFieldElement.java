package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.util.MathUtils;
import java.math.BigInteger;
import java.util.Objects;

public final class MersennePrimeFieldElement implements FieldElement {

  private final BigInteger value;
  private final MersennePrimeModulus modulus;

  private MersennePrimeFieldElement(BigInteger value, MersennePrimeModulus modulus) {
    if (value.signum() < 0) {
      this.value = value.mod(modulus.getBigInteger());
    } else {
      this.value = modulus.mod(value);
    }
    this.modulus = modulus;
  }

  private FieldElement create(BigInteger value) {
    return new MersennePrimeFieldElement(value, modulus);
  }

  static FieldElement create(BigInteger value, MersennePrimeModulus modulus) {
    return new MersennePrimeFieldElement(value, modulus);
  }

  static FieldElement create(int value, MersennePrimeModulus modulus) {
    return create(BigInteger.valueOf(value), modulus);
  }

  static FieldElement create(byte[] bytes, MersennePrimeModulus modulus) {
    return create(new BigInteger(bytes), modulus);
  }

  static FieldElement create(String string, MersennePrimeModulus modulus) {
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
    return create(getModulus().subtract(value));
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

  static BigInteger extractValue(FieldElement element) {
    return ((MersennePrimeFieldElement) element).value;
  }

  byte[] toByteArray() {
    return value.toByteArray();
  }

  private BigInteger getModulus() {
    return modulus.getBigInteger();
  }

  @Override
  public String toString() {
    return "MersennePrimeFieldElement{" +
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
    MersennePrimeFieldElement that = (MersennePrimeFieldElement) o;
    return Objects.equals(modulus, that.modulus) &&
        Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(modulus, value);
  }
}
