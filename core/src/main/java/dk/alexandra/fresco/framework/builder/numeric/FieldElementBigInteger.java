package dk.alexandra.fresco.framework.builder.numeric;

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
  public FieldElementBigInteger subtract(FieldElement operand) {
    return create(value.subtract(operand.convertToBigInteger()));
  }

  @Override
  public FieldElementBigInteger multiply(FieldElement operand) {
    return create(value.multiply(operand.convertToBigInteger()));
  }

  @Override
  public FieldElementBigInteger add(FieldElement operand) {
    return create(value.add(operand.convertToBigInteger()));
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
