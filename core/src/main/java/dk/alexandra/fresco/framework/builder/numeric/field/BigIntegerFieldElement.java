package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.util.MathUtils;
import java.math.BigInteger;
import java.util.Objects;

public class BigIntegerFieldElement implements FieldElement {

  private final BigInteger value;
  private final BigIntegerModulus modulus;

  private BigIntegerFieldElement(BigInteger value, BigIntegerModulus modulus) {
    this.value = value.mod(modulus.getBigInteger());
    this.modulus = modulus;
  }

  private FieldElement create(BigInteger value) {
    return new BigIntegerFieldElement(value, modulus);
  }

  static FieldElement create(BigInteger value, BigIntegerModulus modulus) {
    return new BigIntegerFieldElement(value, modulus);
  }

  static FieldElement create(int value, BigIntegerModulus modulus) {
    return create(BigInteger.valueOf(value), modulus);
  }

  static FieldElement create(byte[] bytes, BigIntegerModulus modulus) {
    return create(new BigInteger(bytes), modulus);
  }

  static FieldElement create(String string, BigIntegerModulus modulus) {
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
    return ((BigIntegerFieldElement) element).value;
  }

  void toByteArray(byte[] bytes, int offset, int byteLength) {
    byte[] byteArray = toByteArray();
    System.arraycopy(byteArray, 0, bytes, byteLength - byteArray.length + offset, byteArray.length);
  }

  byte[] toByteArray() {
    return value.toByteArray();
  }

  private BigInteger getModulus() {
    return modulus.getBigInteger();
  }

  @Override
  public String toString() {
    return "BigIntegerFieldElement{" +
        "value=" + value +
        ", modulus=" + modulus +
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
    BigIntegerFieldElement that = (BigIntegerFieldElement) o;
    return Objects.equals(modulus, that.modulus) &&
        Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(modulus, value);
  }
}
