package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.util.MathUtils;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.Objects;

public class FieldElementBigInteger implements FieldElement {

  private final BigInteger value;
  private final ModulusBigInteger modulus;

  public FieldElementBigInteger(BigInteger value, ModulusBigInteger modulus) {
    this.value = value.mod(modulus.getBigInteger());
    this.modulus = modulus;
  }

  public FieldElementBigInteger(byte[] bytes, ModulusBigInteger modulus) {
    this(new BigInteger(bytes), modulus);
  }

  public FieldElementBigInteger(int value, ModulusBigInteger modulus) {
    this(BigInteger.valueOf(value), modulus);
  }

  public FieldElementBigInteger(String value, ModulusBigInteger modulus) {
    this(new BigInteger(value), modulus);
  }

  private FieldElementBigInteger create(BigInteger divide) {
    return new FieldElementBigInteger(divide, modulus);
  }

  @Override
  public FieldElementBigInteger add(FieldElement operand) {
    return create(value.add(extractValue(operand)));
  }

  @Override
  public FieldElementBigInteger subtract(FieldElement operand) {
    return create(value.subtract(extractValue(operand)));
  }

  @Override
  public FieldElement negate() {
    return create(value.negate());
  }

  @Override
  public FieldElementBigInteger multiply(FieldElement operand) {
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

  @Override
  public boolean isZero() {
    return value.equals(BigInteger.ZERO);
  }

  static BigInteger extractValue(FieldElement element) {
    return ((FieldElementBigInteger) element).value;
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

  @Override
  public BigInteger getModulus() {
    return modulus.getBigInteger();
  }

  @Override
  public String toString() {
    return "FieldElementBigInteger{" +
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
    FieldElementBigInteger that = (FieldElementBigInteger) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
