package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import java.util.Objects;

public class NativeFieldElement implements FieldElement {

  private final BigInteger value;
  private final BigInteger fieldModulus;

  public NativeFieldElement(BigInteger value, BigInteger fieldModulus) {
    this.value = value.mod(fieldModulus);
    this.fieldModulus = fieldModulus;
  }

  @Override
  public FieldElement modInverse(BigInteger operand) {
    return new NativeFieldElement(value.modInverse(operand), fieldModulus);
  }

  @Override
  public FieldElement divide(FieldElement denominator) {
    return create(value.divide(denominator.asBigInteger()));
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
    return create(value.subtract(operand.asBigInteger()));
  }

  @Override
  public NativeFieldElement multiply(FieldElement operand) {
    return create(value.multiply(operand.asBigInteger()));
  }

  @Override
  public NativeFieldElement add(FieldElement operand) {
    return create(value.add(operand.asBigInteger()));
  }

  @Override
  public BigInteger asBigInteger() {
    return value;
  }

  @Override
  public void toByteArray(byte[] bytes, int offset, int byteLength) {
    byte[] byteArray = value.toByteArray();
    System.arraycopy(byteArray, 0, bytes, byteLength - byteArray.length + offset, byteArray.length);
  }

  @Override
  public int compareTo(FieldElement o) {
    return value.compareTo(o.asBigInteger());
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
