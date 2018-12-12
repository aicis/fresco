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

  private FieldElement create(BigInteger value) {
    return new FieldElementBigInteger(value, modulus);
  }

  static FieldElement create(BigInteger value, ModulusBigInteger modulus) {
    return new FieldElementBigInteger(value, modulus);
  }

  static FieldElement create(int value, ModulusBigInteger modulus) {
    return create(BigInteger.valueOf(value), modulus);
  }

  static FieldElement create(byte[] bytes, ModulusBigInteger modulus) {
    return create(new BigInteger(bytes), modulus);
  }

  static FieldElement create(String string, ModulusBigInteger modulus) {
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
    return ((FieldElementBigInteger) element).value;
  }

  void toByteArray(byte[] bytes, int offset, int byteLength) {
    byte[] byteArray = toByteArray();
    System.arraycopy(byteArray, 0, bytes, byteLength - byteArray.length + offset, byteArray.length);
  }

  byte[] toByteArray() {
    return value.toByteArray();
  }

  @Override
  public StrictBitVector toBitVector() {
    int byteLength = getModulus().bitLength() / 8;
    byte[] res = new byte[byteLength];
    byte[] array = value.toByteArray();
    int arrayStart = array.length > byteLength ? array.length - byteLength : 0;
    int resStart = array.length > byteLength ? 0 : byteLength - array.length;
    int len = Math.min(byteLength, array.length);
    System.arraycopy(array, arrayStart, res, resStart, len);
    return new StrictBitVector(res);
  }

  private BigInteger getModulus() {
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
