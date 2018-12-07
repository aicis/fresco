package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import java.util.Objects;

public class NativeFieldElement implements FieldElement {

  private final BigInteger value;
  private final Modulus fieldModulus;

  public NativeFieldElement(BigInteger value, Modulus fieldModulus) {
    this.value = value.mod(fieldModulus.getBigInteger());
    this.fieldModulus = fieldModulus;
  }

  @Override
  public FieldElement divide(FieldElement denominator) {
    return create(value.divide(denominator.convertValueToBigInteger()));
  }

  private NativeFieldElement create(BigInteger divide) {
    return new NativeFieldElement(divide, fieldModulus);
  }

  @Override
  public FieldElement divide(int denominator) {
    return create(value.divide(BigInteger.valueOf(denominator)));
  }

  @Override
  public NativeFieldElement subtract(FieldElement operand) {
    return create(value.subtract(operand.convertValueToBigInteger()));
  }

  @Override
  public NativeFieldElement multiply(FieldElement operand) {
    return create(value.multiply(operand.convertValueToBigInteger()));
  }

  @Override
  public NativeFieldElement add(FieldElement operand) {
    return create(value.add(operand.convertValueToBigInteger()));
  }

  @Override
  public BigInteger convertValueToBigInteger() {
    return value;
  }

  @Override
  public void toByteArray(byte[] bytes, int offset, int byteLength) {
    byte[] byteArray = value.toByteArray();
    System.arraycopy(byteArray, 0, bytes, byteLength - byteArray.length + offset, byteArray.length);
  }

  @Override
  public int compareTo(FieldElement o) {
    return value.compareTo(o.convertValueToBigInteger());
  }

  @Override
  public String toString() {
    return "NativeFieldElement{" +
        "value=" + value +
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
    NativeFieldElement that = (NativeFieldElement) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
